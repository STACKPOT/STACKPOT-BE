package stackpot.stackpot.feed.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.FeedHandler;
import stackpot.stackpot.apiPayload.exception.handler.UserHandler;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.feed.converter.FeedConverter;
import stackpot.stackpot.feed.dto.FeedResponseDto;
import stackpot.stackpot.feed.entity.Feed;
import stackpot.stackpot.feed.entity.Series;
import stackpot.stackpot.feed.entity.enums.Category;
import stackpot.stackpot.feed.entity.enums.Interest;
import stackpot.stackpot.feed.repository.FeedCommentRepository;
import stackpot.stackpot.feed.repository.FeedLikeRepository;
import stackpot.stackpot.feed.repository.FeedRepository;
import stackpot.stackpot.feed.repository.SeriesRepository;
import stackpot.stackpot.notification.service.NotificationCommandService;
import stackpot.stackpot.save.converter.FeedSaveRepository;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedQueryServiceImpl implements FeedQueryService {

    private final NotificationCommandService notificationCommandService;
    private final FeedRepository feedRepository;
    private final FeedConverter feedConverter;
    private final UserRepository userRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final SeriesRepository seriesRepository;
    private final AuthService authService;
    private final FeedSaveRepository feedSaveRepository;
    private final FeedCommentRepository feedCommentRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public FeedResponseDto.FeedPreviewList getPreViewFeeds(String categoryStr, String sort, Long cursor, int limit) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null &&
                !(authentication instanceof AnonymousAuthenticationToken) &&
                authentication.isAuthenticated();

        log.info("isAuthenticated :{}", isAuthenticated);

        final User user = isAuthenticated
                ? userRepository.findByUserId(authService.getCurrentUserId()).orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND))
                : null;

        final Long userId = (user != null) ? user.getId() : null;
        final List<Long> likedFeedIds = (userId != null)
                ? feedLikeRepository.findFeedIdsByUserId(userId)
                : List.of();
        final List<Long> savedFeedIds = (userId != null)
                ? feedSaveRepository.findFeedIdsByUserId(userId)
                : List.of();

        Long lastFeedId = Long.MAX_VALUE;
        Long lastFeedLike = 0L;

        if (cursor != null) {
            lastFeedId = cursor;
            Feed lastFeed = feedRepository.findById(lastFeedId)
                    .orElseThrow(() -> new FeedHandler(ErrorStatus.FEED_NOT_FOUND));
            lastFeedLike = lastFeed.getLikeCount();
        } else if ("old".equals(sort)) {
            lastFeedId = 0L;
        } else if ("popular".equals(sort)) {
            lastFeedLike = Long.MAX_VALUE;
        }

        Category category = null;
        if (categoryStr != null && !categoryStr.isEmpty()) {
            if (!categoryStr.equalsIgnoreCase("ALL")) {
                try {
                    category = Category.valueOf(categoryStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid category string: {}", categoryStr);
                    category = null;
                }
            }
        }

        Pageable pageable = PageRequest.ofSize(limit);
        List<Feed> feedResults;

        Interest userInterestEnum = null;
        if (user != null && user.getInterest() != null) {
            try {
                userInterestEnum = Interest.fromLabel(user.getInterest());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid user interest: {}", user.getInterest());
            }
        }

        if (userInterestEnum != null) {
            sort = "popular"; // 강제로 인기순
            lastFeedLike = (cursor != null) ? lastFeedLike : Long.MAX_VALUE;

            feedResults = feedRepository.findFeedsByInterestAndCategoryWithCursor(
                    userInterestEnum, category, lastFeedLike, lastFeedId, pageable
            );
        } else {
            feedResults = feedRepository.findFeeds(category, sort, lastFeedId, lastFeedLike, pageable);
        }


        List<FeedResponseDto.FeedDto> feedDtoList = feedResults.stream()
                .map(feed -> {
                    boolean isOwner = (user != null) && Objects.equals(user.getId(), feed.getUser().getUserId());
                    Boolean isLiked = (isAuthenticated && userId != null) ? likedFeedIds.contains(feed.getFeedId()) : null;
                    Boolean isSaved = (isAuthenticated && userId != null) ? savedFeedIds.contains(feed.getFeedId()) : null;
                    int saveCount = feedSaveRepository.countByFeed(feed);

                    return feedConverter.feedDto(feed, isOwner, isLiked, isSaved, saveCount);
                })
                .collect(Collectors.toList());

        Long nextCursor = null;

        if (!feedResults.isEmpty() && feedResults.size() == limit) {
            Feed lastFeed = feedResults.get(feedResults.size() - 1);
            nextCursor = lastFeed.getFeedId();

            List<Feed> nextFeedResults;
            if (userInterestEnum != null) {
                nextFeedResults = feedRepository.findFeedsByInterestAndCategoryWithCursor(
                        userInterestEnum, category, lastFeed.getLikeCount(), nextCursor, pageable
                );
            } else {
                nextFeedResults = feedRepository.findFeeds(category, sort, nextCursor, lastFeedLike, pageable);
            }

            if (nextFeedResults.isEmpty()) {
                nextCursor = null;
            }

        }

        return new FeedResponseDto.FeedPreviewList(feedDtoList, nextCursor);
    }


    @Override
    public FeedResponseDto.AuthorizedFeedDto getFeed(Long feedId) {
        User user = authService.getCurrentUser();

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedHandler(ErrorStatus.FEED_NOT_FOUND));

        boolean isOwner = feed.getUser().getId().equals(user.getId());
        boolean isLiked = feedLikeRepository.existsByFeedAndUser(feed, user);

        List<Long> savedFeedIds = feedSaveRepository.findFeedIdsByUserId(user.getId());
        boolean isSaved = savedFeedIds.contains(feed.getFeedId());

        Long commentCount = feedCommentRepository.countByFeedId(feed.getFeedId());

        return feedConverter.toAuthorizedFeedDto(feed, isOwner, isLiked, isSaved, commentCount);
    }

    @Transactional
    public FeedResponseDto.FeedPreviewList getFeedsByUserId(Long userId, Long nextCursor, int pageSize) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.isAuthenticated();

        final User loginUser = isAuthenticated
                ? userRepository.findByUserId(authService.getCurrentUserId())
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND))
                : null;

        final Long loginUserId = (loginUser != null) ? loginUser.getId() : null;

        final List<Long> likedFeedIds = (loginUserId != null)
                ? feedLikeRepository.findFeedIdsByUserId(loginUserId)
                : List.of();

        final List<Long> savedFeedIds = (loginUserId != null)
                ? feedSaveRepository.findFeedIdsByUserId(loginUserId)
                : List.of();

        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "feedId"));
        List<Feed> feeds;

        if (nextCursor == null) {
            feeds = feedRepository.findByUser_Id(userId, pageable);
        } else {
            feeds = feedRepository.findByUserIdAndFeedIdBefore(userId, nextCursor, pageable);
        }

        List<FeedResponseDto.FeedDto> feedDtos = feeds.stream()
                .map(feed -> {
                    boolean isOwner = loginUserId != null && Objects.equals(loginUserId, feed.getUser().getId());
                    Boolean isLiked = (loginUserId != null) ? likedFeedIds.contains(feed.getFeedId()) : null;
                    Boolean isSaved = (loginUserId != null) ? savedFeedIds.contains(feed.getFeedId()) : null;
                    int saveCount = feedSaveRepository.countByFeed(feed);

                    return feedConverter.feedDto(feed, isOwner, isLiked, isSaved, saveCount);
                })
                .collect(Collectors.toList());

        Long nextCursorResult = (!feeds.isEmpty() && feeds.size() >= pageSize)
                ? feeds.get(feeds.size() - 1).getFeedId()
                : null;

        return FeedResponseDto.FeedPreviewList.builder()
                .feeds(feedDtos)
                .nextCursor(nextCursorResult)
                .build();
    }

    @Override
    public FeedResponseDto.FeedPreviewList getFeeds(Long nextCursor, int pageSize) {
        User user = authService.getCurrentUser();

        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "feedId"));
        List<Feed> feeds = (nextCursor == null)
                ? feedRepository.findByUser_Id(user.getId(), pageable)
                : feedRepository.findByUserIdAndFeedIdBefore(user.getId(), nextCursor, pageable);

        List<Long> likedFeedIds = feedLikeRepository.findFeedIdsByUserId(user.getId());
        List<Long> savedFeedIds = feedSaveRepository.findFeedIdsByUserId(user.getId());

        List<FeedResponseDto.FeedDto> feedDtos = feeds.stream()
                .map(feed -> {
                    boolean isOwner = true;
                    Boolean isLiked = likedFeedIds.contains(feed.getFeedId());
                    Boolean isSaved = savedFeedIds.contains(feed.getFeedId());
                    int saveCount = feedSaveRepository.countByFeed(feed);

                    return feedConverter.feedDto(feed, isOwner, isLiked, isSaved, saveCount);
                })
                .collect(Collectors.toList());

        Long nextCursorResult = (!feeds.isEmpty() && feeds.size() >= pageSize)
                ? feeds.get(feeds.size() - 1).getFeedId()
                : null;

        return FeedResponseDto.FeedPreviewList.builder()
                .feeds(feedDtos)
                .nextCursor(nextCursorResult)
                .build();
    }


    @Override
    public Long getLikeCount(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedHandler(ErrorStatus.FEED_NOT_FOUND));
        return feedLikeRepository.countByFeed(feed);
    }

    @Override
    public Feed getFeedByFeedId(Long feedId) {
        return feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedHandler(ErrorStatus.FEED_NOT_FOUND));
    }


    @Override
    public Map<Long, String> getMySeries() {
        User user = authService.getCurrentUser();

        List<Series> userSeriesList = seriesRepository.findAllByUser(user);

        return userSeriesList.stream()
                .collect(Collectors.toMap(
                        Series::getSeriesId,
                        Series::getComment
                ));
    }

    @Override
    public Map<String, Object> getLikedFeedsWithPaging(int page, int size) {
        User user = authService.getCurrentUser(); // 인증 필요

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Feed> feedPage = feedLikeRepository.findLikedFeedsByUserId(user.getId(), pageable); // 저장된 피드들 조회

        List<Feed> feeds = feedPage.getContent();
        List<Long> feedIds = feeds.stream()
                .map(Feed::getFeedId)
                .collect(Collectors.toList());

        // 미리 좋아요한 피드 ID 조회
        List<Long> likedFeedIds = feedLikeRepository.findFeedIdsByUserId(user.getId());

        // 저장 수 조회
        List<Object[]> rawResults = feedSaveRepository.countSavesByFeedIds(feedIds);
        Map<Long, Integer> saveCountMap = rawResults.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Long) row[1]).intValue()
                ));

        // DTO 변환
        List<FeedResponseDto.FeedDto> content = feeds.stream()
                .map(feed -> {
                    Long feedId = feed.getFeedId();
                    boolean isSaved = true;
                    boolean isLiked = likedFeedIds.contains(feedId);
                    boolean isOwner = feed.getUser().getId().equals(user.getId());
                    int saveCount = saveCountMap.getOrDefault(feedId, 0);

                    return feedConverter.feedDto(feed, isOwner, isLiked, isSaved, saveCount);
                })
                .collect(Collectors.toList());

        // 결과 Map 생성
        Map<String, Object> response = new HashMap<>();
        response.put("feeds", content);
        response.put("currentPage", feedPage.getNumber() + 1);
        response.put("totalPages", feedPage.getTotalPages());
        response.put("totalElements", feedPage.getTotalElements());
        response.put("size", feedPage.getSize());

        return response;
    }

}