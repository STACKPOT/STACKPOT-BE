package stackpot.stackpot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import stackpot.stackpot.converter.FeedConverter;
import stackpot.stackpot.domain.Feed;
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
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final FeedRepository feedRepository;
    private final FeedConverter feedConverter;
    private final UserRepository userRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final FeedSaveRepository feedSaveRepository;

    @Override
    public FeedResponseDto.FeedPreviewList getPreViewFeeds(Category categor, String sort, String cursor, int limit) {
        // 커서가 없으면 현재 시간 사용
        LocalDateTime lastCreatedAt = cursor != null
                ? LocalDateTime.parse(cursor)
                : LocalDateTime.now();

        // Pageable 생성
        Pageable pageable = PageRequest.of(0, limit);

        // 데이터 조회
        List<Object[]> feedResults = feedRepository.findFeeds(categor, sort, lastCreatedAt, pageable);

        // Feed와 인기 점수를 DTO로 변환
        List<FeedResponseDto.FeedDto> feedDtoList = feedResults.stream()
                .map(result -> {
                    Feed feed = (Feed) result[0];
                    int popularity = (int) result[1];
                    int likeCount = (int) result[2];

                    return feedConverter.feedDto(feed, popularity, likeCount);
                })
                .collect(Collectors.toList());

        // 다음 커서 계산
        String nextCursor = feedResults.isEmpty()
                ? null
                : ((Feed) feedResults.get(feedResults.size() - 1)[0]).getCreatedAt().toString();

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
        if(request.getVisibility() != null){
            feed.setVisibility(request.getVisibility());
        }
        if(request.getCategor() != null){
            feed.setCategory(request.getCategor());
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

    @Override
    public boolean toggleSave(Long feedId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Optional<FeedSave> existingSave = feedSaveRepository.findByFeedAndUser(feed, user);

        if (existingSave.isPresent()) {
            feedSaveRepository.delete(existingSave.get());
            return false;
        } else {
            FeedSave feedSave = FeedSave.builder()
                    .feed(feed)
                    .user(user)
                    .build();

            feedSaveRepository.save(feedSave);
            return true; // 좋아요 성공
        }
    }

    @Override
    public Long getSaveCount(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));
        return feedSaveRepository.countByFeed(feed);
    }

    @Override
    public Long getLikeCount(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));
        return feedLikeRepository.countByFeed(feed);
    }
}