package stackpot.stackpot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import stackpot.stackpot.repository.FeedLikeRepository;
import stackpot.stackpot.repository.FeedRepository.FeedRepository;
import stackpot.stackpot.repository.UserRepository.UserRepository;
import stackpot.stackpot.web.dto.FeedRequestDto;
import stackpot.stackpot.web.dto.FeedResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static stackpot.stackpot.domain.enums.Category.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final FeedRepository feedRepository;
    private final FeedConverter feedConverter;
    private final UserRepository userRepository;
    private final FeedLikeRepository feedLikeRepository;


    @Override
    public FeedResponseDto.FeedPreviewList getPreViewFeeds(String categoryStr, String sort, Long cursor, int limit) {
        Long lastFeedId = Long.MAX_VALUE;  // 기본적으로 가장 큰 ID부터 조회
        Long lastFeedLike = 0L;

        if ( cursor != null ) {
            lastFeedId = cursor;
            Feed lastdFeed = feedRepository.findById(lastFeedId)
                    .orElseThrow(()-> new IllegalArgumentException("feed를 찾을 수 없습니다."));

            lastFeedLike = lastdFeed.getLikeCount();
        }
        else if (sort.equals("old")) {
            lastFeedId = 0L;
        } else if (sort.equals("popular")) {
            lastFeedLike = Long.MAX_VALUE;
        }

        Category category = null;
        if (categoryStr != null && !categoryStr.isEmpty()) {
            if (categoryStr.equalsIgnoreCase("ALL")) {
                category = null;
            } else {
                try {
                    category = Category.valueOf(categoryStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    category = null;
                }
            }
        }
        Pageable pageable = PageRequest.ofSize(limit);

        List<Feed> feedResults = feedRepository.findFeeds(category, sort, lastFeedId, lastFeedLike, pageable);

        List<FeedResponseDto.FeedDto> feedDtoList = feedResults.stream()
                .map(feed -> feedConverter.feedDto(feed))
                .collect(Collectors.toList());

        Long nextCursor = null;
        if (!feedResults.isEmpty() && feedResults.size() >= limit) {
            Feed lastFeed = feedResults.get(feedResults.size() - 1);
            nextCursor = lastFeed.getFeedId();  // 🔹 int 값으로 변환하여 반환
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
    public FeedResponseDto.FeedPreviewList getFeedsByUserId(Long userId, Long nextCursor, int pageSize) {
        // 피드 조회 (페이징 처리 추가)
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "feedId"));
        List<Feed> feeds;

        if (nextCursor == null) {
            // 첫 페이지 조회
            feeds = feedRepository.findByUser_Id(userId, pageable);
        } else {
            // 다음 페이지 조회 (Cursor 기반 페이징)
            Long cursorFeedId = nextCursor;
            feeds = feedRepository.findByUserIdAndFeedIdBefore(userId, cursorFeedId, pageable);
        }

        // Feed -> FeedDto 변환 (FeedConverter 활용)
        List<FeedResponseDto.FeedDto> feedDtos = feeds.stream()
                .map(feed -> feedConverter.feedDto(feed))
                .collect(Collectors.toList());

        // 다음 커서 설정 (마지막 피드의 createdAt)
        Long nextCursorResult = (!feeds.isEmpty() && feeds.size() >= pageSize) ? feeds.get(feeds.size() - 1).getFeedId() : null ;



        return FeedResponseDto.FeedPreviewList.builder()
                .feeds(feedDtos)
                .nextCursor( nextCursorResult )
                .build();
    }

    @Override
    public FeedResponseDto.FeedPreviewList getFeeds(Long nextCursor, int pageSize) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 피드 조회 (페이징 처리 추가)
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "feedId"));
        List<Feed> feeds;

        if (nextCursor == null) {
            // 첫 페이지 조회
            feeds = feedRepository.findByUser_Id(user.getId(), pageable);
        } else {
            // 다음 페이지 조회 (Cursor 기반 페이징)
            Long cursorFeedId = nextCursor;
            feeds = feedRepository.findByUserIdAndFeedIdBefore(user.getId(), cursorFeedId, pageable);

        }

        // Feed -> FeedDto 변환 (FeedConverter 활용)
        List<FeedResponseDto.FeedDto> feedDtos = feeds.stream()
                .map(feed -> feedConverter.feedDto(feed))
                .collect(Collectors.toList());

        // 다음 커서 설정 (마지막 피드의 createdAt)
        Long nextCursorResult = (!feeds.isEmpty() && feeds.size() >= pageSize) ? feeds.get(feeds.size() - 1).getFeedId() : null ;

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
            feed.setLikeCount(feed.getLikeCount()-1);
            feedRepository.save(feed);

            return false; // 좋아요 취소
        } else {
            // 좋아요 추가
            FeedLike feedLike = FeedLike.builder()
                    .feed(feed)
                    .user(user)
                    .build();
            feedLikeRepository.save(feedLike);
            feed.setLikeCount(feed.getLikeCount()+1);
            feedRepository.save(feed);
            return true; // 좋아요 성공
        }
    }

    @Override
    public Long getLikeCount(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));
        return feedLikeRepository.countByFeed(feed);
    }
}