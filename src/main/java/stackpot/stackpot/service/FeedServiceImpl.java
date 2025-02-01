package stackpot.stackpot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.MemberHandler;
import stackpot.stackpot.apiPayload.exception.handler.PotHandler;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.MemberHandler;
import stackpot.stackpot.converter.FeedConverter;
import stackpot.stackpot.domain.Feed;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.Category;
import stackpot.stackpot.domain.mapping.FeedLike;
import stackpot.stackpot.domain.mapping.FeedSave;
import stackpot.stackpot.repository.FeedLikeRepository;
import stackpot.stackpot.repository.FeedRepository.FeedRepository;
import stackpot.stackpot.repository.FeedSaveRepository;
import stackpot.stackpot.repository.UserRepository.UserRepository;
import stackpot.stackpot.web.dto.FeedRequestDto;
import stackpot.stackpot.web.dto.FeedResponseDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static stackpot.stackpot.domain.enums.Category.*;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final FeedRepository feedRepository;
    private final FeedConverter feedConverter;
    private final UserRepository userRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final FeedSaveRepository feedSaveRepository;

    @Override
    public FeedResponseDto.FeedPreviewList getPreViewFeeds(String categoryStr, String sort, String cursor, int limit) {
        // 커서가 없으면 현재 시간 사용
        LocalDateTime lastCreatedAt = (cursor != null && !cursor.isEmpty())
                ? LocalDateTime.parse(cursor)
                : LocalDateTime.now();

        // Pageable 객체 생성 (페이지 번호 없이 크기만 설정)
        Pageable pageable = PageRequest.ofSize(limit);

        Category category = null;
        if (categoryStr != null && !categoryStr.isEmpty()) {
            if (categoryStr.equalsIgnoreCase("ALL")) {
                category = null; // 전체 카테고리는 필터링 없이 조회
            } else {
                try {
                    category = Category.valueOf(categoryStr.toUpperCase()); // 안전한 변환
                } catch (IllegalArgumentException e) {
                    category = null; // 잘못된 값이면 전체 조회
                }
            }
        }

        // 데이터 조회
        List<Object[]> feedResults = feedRepository.findFeeds(category, sort, lastCreatedAt, pageable);

        // Feed와 인기 점수를 DTO로 변환
        List<FeedResponseDto.FeedDto> feedDtoList = feedResults.stream()
                .map(result -> {
                    Feed feed = (Feed) result[0];
                    int likeCount = ((Number) result[1]).intValue();  // 안전한 형변환

                    return feedConverter.feedDto(feed, likeCount);
                })
                .collect(Collectors.toList());

        // 다음 커서 계산
        String nextCursor = null;
        if (!feedResults.isEmpty()) {
            Feed lastFeed = (Feed) feedResults.get(feedResults.size() - 1)[0];

            // 인기순 정렬일 경우, likeCount와 createdAt을 함께 커서로 사용
            nextCursor = sort.equals("popular")
                    ? getLikeCount(lastFeed.getFeedId()) + "|" + lastFeed.getCreatedAt().toString()
                    : lastFeed.getCreatedAt().toString();
        }

        return new FeedResponseDto.FeedPreviewList(feedDtoList, nextCursor);
    }

    @Override
    public Feed createFeed(FeedRequestDto.createDto request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Feed feed = feedConverter.toFeed(request);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        feed.setUser(user);
        return feedRepository.save(feed);

    }

    @Override
    public Feed getFeed(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(()->new IllegalArgumentException("해당 피드를 찾을 수 없습니다."));
        return feed;
    }

    @Transactional
    public FeedResponseDto.FeedPreviewList getFeedsByUserId(Long userId, String nextCursor, int pageSize) {
        // 피드 조회 (페이징 처리 추가)
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Feed> feeds;

        if (nextCursor == null || nextCursor.isBlank()) {
            // 첫 페이지 조회
            feeds = feedRepository.findByUser_Id(userId, pageable);
        } else {
            // 다음 페이지 조회 (Cursor 기반 페이징)
            LocalDateTime cursorTime = LocalDateTime.parse(nextCursor);
            feeds = feedRepository.findByUserIdAndCreatedAtBefore(userId, cursorTime, pageable);
        }

        // Feed -> FeedDto 변환 (FeedConverter 활용)
        List<FeedResponseDto.FeedDto> feedDtos = feeds.stream()
                .map(feed -> feedConverter.feedDto(feed, feedLikeRepository.countByFeed(feed)))
                .collect(Collectors.toList());

        // 다음 커서 설정 (마지막 피드의 createdAt)
        String nextCursorResult = feeds.isEmpty() ? null : feeds.get(feeds.size() - 1).getCreatedAt().toString();

        return FeedResponseDto.FeedPreviewList.builder()
                .feeds(feedDtos)
                .nextCursor(nextCursorResult)
                .build();
    }

    @Override
    public FeedResponseDto.FeedPreviewList getFeeds(String nextCursor, int pageSize) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 피드 조회 (페이징 처리 추가)
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Feed> feeds;

        if (nextCursor == null || nextCursor.isBlank()) {
            // 첫 페이지 조회
            feeds = feedRepository.findByUser_Id(user.getId(), pageable);
        } else {
            // 다음 페이지 조회 (Cursor 기반 페이징)
            LocalDateTime cursorTime = LocalDateTime.parse(nextCursor);
            feeds = feedRepository.findByUserIdAndCreatedAtBefore(user.getId(), cursorTime, pageable);
        }

        // Feed -> FeedDto 변환 (FeedConverter 활용)
        List<FeedResponseDto.FeedDto> feedDtos = feeds.stream()
                .map(feed -> feedConverter.feedDto(feed, feedLikeRepository.countByFeed(feed)))
                .collect(Collectors.toList());

        // 다음 커서 설정 (마지막 피드의 createdAt)
        String nextCursorResult = feeds.isEmpty() ? null : feeds.get(feeds.size() - 1).getCreatedAt().toString();

        return FeedResponseDto.FeedPreviewList.builder()
                .feeds(feedDtos)
                .nextCursor(nextCursorResult)
                .build();
    }

    @Override
    public Feed modifyFeed(long feedId, FeedRequestDto.createDto request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("해당 피드를 찾을 수 없습니다."));

        if(!feed.getUser().getEmail().equals(email)){
            throw new SecurityException("해당 피드를 수정할 권한이 없습니다.");
        }

        if(request.getTitle() != null){
            feed.setTitle(request.getTitle());
        }
        if(request.getContent() != null){
            feed.setContent(request.getContent());
        }
        if(request.getCategory() != null){
            feed.setCategory(request.getCategory());
        }
        return feedRepository.save(feed);
    }

    @Override
    public boolean toggleLike(Long feedId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Optional<FeedLike> existingLike = feedLikeRepository.findByFeedAndUser(feed, user);

        if (existingLike.isPresent()) {
            // 이미 좋아요가 있다면 삭제 (좋아요 취소)
            feedLikeRepository.delete(existingLike.get());
            return false; // 좋아요 취소
        } else {
            // 좋아요 추가
            FeedLike feedLike = FeedLike.builder()
                    .feed(feed)
                    .user(user)
                    .build();

            feedLikeRepository.save(feedLike);
            return true; // 좋아요 성공
        }
    }

//    @Override
//    public boolean toggleSave(Long feedId) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String userEmail = authentication.getName();
//
//        Feed feed = feedRepository.findById(feedId)
//                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));
//
//        User user = userRepository.findByEmail(userEmail)
//                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
//
//        Optional<FeedSave> existingSave = feedSaveRepository.findByFeedAndUser(feed, user);
//
//        if (existingSave.isPresent()) {
//            feedSaveRepository.delete(existingSave.get());
//            return false;
//        } else {
//            FeedSave feedSave = FeedSave.builder()
//                    .feed(feed)
//                    .user(user)
//                    .build();
//
//            feedSaveRepository.save(feedSave);
//            return true; // 좋아요 성공
//        }
//    }

//    @Override
//    public Long getSaveCount(Long feedId) {
//        Feed feed = feedRepository.findById(feedId)
//                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));
//        return feedSaveRepository.countByFeed(feed);
//    }

    @Override
    public Long getLikeCount(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));
        return feedLikeRepository.countByFeed(feed);
    }
}