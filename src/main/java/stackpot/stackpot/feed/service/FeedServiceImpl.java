package stackpot.stackpot.feed.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.FeedHandler;
import stackpot.stackpot.apiPayload.exception.handler.MemberHandler;
import stackpot.stackpot.apiPayload.exception.handler.UserHandler;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.feed.converter.FeedConverter;
import stackpot.stackpot.feed.entity.Feed;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.feed.entity.enums.Category;
import stackpot.stackpot.feed.entity.mapping.FeedLike;
import stackpot.stackpot.feed.repository.FeedLikeRepository;
import stackpot.stackpot.feed.repository.FeedRepository;
import stackpot.stackpot.user.repository.UserRepository;
import stackpot.stackpot.feed.dto.FeedRequestDto;
import stackpot.stackpot.feed.dto.FeedResponseDto;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final FeedRepository feedRepository;
    private final FeedConverter feedConverter;
    private final UserRepository userRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final AuthService authService;

    @Override
    public FeedResponseDto.FeedPreviewList getPreViewFeeds(String categoryStr, String sort, Long cursor, int limit) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.isAuthenticated();

        log.info("isAuthenticated :{}",isAuthenticated);

        final User user = isAuthenticated
                ? userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND))
                : null;

        final Long userId = (user != null) ? user.getId() : null;
        final List<Long> likedFeedIds = (userId != null)
                ? feedLikeRepository.findFeedIdsByUserId(userId)
                : List.of(); // 비로그인 사용자는 빈 리스트

        Long lastFeedId = Long.MAX_VALUE;  // 기본적으로 가장 큰 ID부터 조회
        Long lastFeedLike = 0L;

        if ( cursor != null ) {
            lastFeedId = cursor;
            Feed lastdFeed = feedRepository.findById(lastFeedId)
                    .orElseThrow(()-> new FeedHandler(ErrorStatus.FEED_NOT_FOUND));

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
                .map(feed -> {
                    boolean isOwner = (user != null) && Objects.equals(user.getId(), feed.getUser().getUserId());

                    Boolean isLiked = (isAuthenticated && userId != null)
                            ? likedFeedIds.contains(feed.getFeedId())
                            : null; // 비로그인 사용자는 null 처리

                    FeedResponseDto.FeedDto feedDto = feedConverter.feedDto(feed);
                    feedDto.setIsLiked(isLiked); // 좋아요 상태 추가
                    return feedDto;
                })
                .collect(Collectors.toList());

        Long nextCursor = null;

        if (!feedResults.isEmpty() && feedResults.size() == limit) {
            Feed lastFeed = feedResults.get(feedResults.size() - 1);
            nextCursor = lastFeed.getFeedId();
            List<Feed> nextfeedResults = feedRepository.findFeeds(category, sort, nextCursor, lastFeedLike, pageable);

            if(nextfeedResults.size() == 0){
                nextCursor = null;
            }
        }
        return new FeedResponseDto.FeedPreviewList(feedDtoList, nextCursor);
    }

    @Override
    public FeedResponseDto.FeedDto createFeed(FeedRequestDto.createDto request) {
        Feed feed = feedConverter.toFeed(request);
        User user = authService.getCurrentUser();

        feed.setUser(user);
        FeedResponseDto.FeedDto response = feedConverter.feedDto(feedRepository.save(feed));
        return response;
    }

    @Override
    public FeedResponseDto.FeedDto getFeed(Long feedId) {

        User user = authService.getCurrentUser();
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(()-> new FeedHandler(ErrorStatus.FEED_NOT_FOUND));

        boolean isOwner = Objects.equals(user.getId(), feed.getUser().getUserId());

        FeedResponseDto.FeedDto response = feedConverter.toAuthorizedFeedDto(feed, isOwner);

        return response;
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
        User user = authService.getCurrentUser();

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
    public FeedResponseDto.FeedDto modifyFeed(long feedId, FeedRequestDto.createDto request) {
        User user = authService.getCurrentUser();

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedHandler(ErrorStatus.FEED_NOT_FOUND));

        if(!feed.getUser().getEmail().equals(user.getEmail())){
            throw new FeedHandler(ErrorStatus.FEED_UNAUTHORIZED);
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
        FeedResponseDto.FeedDto response = feedConverter.feedDto(feedRepository.save(feed));
        return response;
    }

    @Override
    public String deleteFeed(Long feedId) {
        User user = authService.getCurrentUser();

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedHandler(ErrorStatus.FEED_NOT_FOUND));

        if(!feed.getUser().getEmail().equals(user.getEmail())){
            throw new FeedHandler(ErrorStatus.FEED_UNAUTHORIZED);
        }

        feedRepository.delete(feed);

        return "피드를 삭제했습니다.";
    }

    @Override
    public boolean toggleLike(Long feedId) {
        User user = authService.getCurrentUser();

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedHandler(ErrorStatus.FEED_NOT_FOUND));
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
                .orElseThrow(() -> new FeedHandler(ErrorStatus.FEED_NOT_FOUND));
        return feedLikeRepository.countByFeed(feed);
    }
}