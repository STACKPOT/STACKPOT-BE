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
import stackpot.stackpot.common.util.RedisUtil;
import stackpot.stackpot.feed.converter.FeedConverter;
import stackpot.stackpot.feed.dto.FeedCacheDto;
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
import stackpot.stackpot.save.repository.FeedSaveRepository;
import stackpot.stackpot.user.dto.response.UserMyPageResponseDto;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.repository.UserRepository;

import java.util.*;
import java.util.concurrent.TimeUnit;
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
    private final RedisUtil redisUtil;
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

        List<Interest> userInterestEnumList = null;
        if (user != null && user.getInterests() != null && !user.getInterests().isEmpty()) {
            try {
                userInterestEnumList = Interest.fromLabels(user.getInterests()); // 여러 관심사 처리
            } catch (IllegalArgumentException e) {
                log.warn("Invalid user interest: {}", user.getInterests());
            }
        }

        // 관심사가 여러 개 있을 경우
        if (userInterestEnumList != null && !userInterestEnumList.isEmpty()) {
            sort = "popular"; // 강제로 인기순
            lastFeedLike = (cursor != null) ? lastFeedLike : Long.MAX_VALUE;

            feedResults = feedRepository.findFeedsByInterestsAndCategoryWithCursor(
                    userInterestEnumList, category, lastFeedLike, lastFeedId, pageable
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
            if (userInterestEnumList != null && !userInterestEnumList.isEmpty()) {
                nextFeedResults = feedRepository.findFeedsByInterestsAndCategoryWithCursor(
                        userInterestEnumList, category, lastFeed.getLikeCount(), nextCursor, pageable
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
    public UserMyPageResponseDto getFeedsByUserId(Long userId, Long nextCursor, int pageSize, Long seriesId) {
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
//        List<Feed> feeds = (nextCursor == null)
//                ? feedRepository.findByUser_Id(userId, pageable)
//                : feedRepository.findByUserIdAndFeedIdBefore(userId, nextCursor, pageable);
        List<Feed> feeds;
        if (seriesId == 0L) {
            feeds = (nextCursor == null)
                    ? feedRepository.findByUser_Id(userId, pageable)
                    : feedRepository.findByUserIdAndFeedIdBefore(userId, nextCursor, pageable);
        } else {
            feeds = (nextCursor == null)
                    ? feedRepository.findByUser_IdAndSeries_SeriesId(userId, seriesId, pageable)
                    : feedRepository.findByUser_IdAndSeries_SeriesIdAndFeedIdBefore(userId, seriesId, nextCursor, pageable);
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

        // 조회 대상 유저 정보
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        List<String> seriesComments = targetUser.getSeriesList().stream()
                .map(Series::getComment)
                .collect(Collectors.toList());

        return UserMyPageResponseDto.builder()
                .id(targetUser.getId())
                .seriesComments(seriesComments)
                .feeds(feedDtos)
                .build();
    }

    @Override
    public UserMyPageResponseDto getFeeds(Long nextCursor, int pageSize, Long seriesId) {
        User user = authService.getCurrentUser();

        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "feedId"));
        List<Feed> feeds;
        if (seriesId == 0L) {
            feeds = (nextCursor == null)
                    ? feedRepository.findByUser_Id(user.getId(), pageable)
                    : feedRepository.findByUserIdAndFeedIdBefore(user.getId(), nextCursor, pageable);
        } else {
            feeds = (nextCursor == null)
                    ? feedRepository.findByUser_IdAndSeries_SeriesId(user.getId(), seriesId, pageable)
                    : feedRepository.findByUser_IdAndSeries_SeriesIdAndFeedIdBefore(user.getId(), seriesId, nextCursor, pageable);
        }

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

        List<String> seriesComments = user.getSeriesList().stream()
                .map(Series::getComment)
                .collect(Collectors.toList());

        return UserMyPageResponseDto.builder()
                .id(user.getId())
                .seriesComments(seriesComments)
                .feeds(feedDtos)
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
    public Map<Long, String> getOtherSeries(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

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


    @Override
    public FeedResponseDto.FeedPreviewList searchMyFeeds(Long nextCursor, int pageSize, String keyword) {
        User user = authService.getCurrentUser();

        // 1. DB에서 로그인 유저의 피드 전체 ID 가져오기 (커서 기반)
        List<Feed> dbFeeds = feedRepository.findMyFeedsByCursor(user.getId(), nextCursor, PageRequest.of(0, pageSize * 2));

        List<FeedCacheDto> cachedFeeds = new ArrayList<>();

        for (Feed feed : dbFeeds) {
            String cacheKey = "feed:" + feed.getFeedId();
            FeedCacheDto cached = redisUtil.get(cacheKey, FeedCacheDto.class);
            if (cached != null) {
                cachedFeeds.add(cached);
            } else {
                FeedCacheDto dto = feedConverter.toFeedCacheDto(feed);
                redisUtil.set(cacheKey, dto, 30, TimeUnit.MINUTES);
                cachedFeeds.add(dto);
            }
        }

        // 2. 키워드 필터링
        List<FeedCacheDto> filtered = cachedFeeds.stream()
                .filter(feed -> {
                    if (keyword == null || keyword.isBlank()) return true;
                    String lower = keyword.toLowerCase();
                    return (feed.getTitle() != null && feed.getTitle().toLowerCase().contains(lower)) ||
                            (feed.getContent() != null && feed.getContent().toLowerCase().contains(lower));
                })
                .sorted(Comparator.comparing(FeedCacheDto::getFeedId).reversed())
                .limit(pageSize)
                .toList();

        List<Long> likedIds = feedLikeRepository.findFeedIdsByUserId(user.getId());
        List<Long> savedIds = feedSaveRepository.findFeedIdsByUserId(user.getId());

        List<FeedResponseDto.FeedDto> feedDtos = filtered.stream()
                .map(feed -> {
                    boolean isLiked = likedIds.contains(feed.getFeedId());
                    boolean isSaved = savedIds.contains(feed.getFeedId());
                    long likeCount = feedLikeRepository.countByFeedId(feed.getFeedId());
                    long saveCount = feedSaveRepository.countByFeedId(feed.getFeedId());
                    long commentCount = 0L;
                    boolean isOwner = feed.getUserId().equals(user.getId());

                    return feedConverter.toFeedDtoFromCache(feed, isLiked, isSaved, likeCount, saveCount, commentCount, isOwner);
                })
                .toList();

        Long newCursor = filtered.isEmpty() ? null : filtered.get(filtered.size() - 1).getFeedId();

        return FeedResponseDto.FeedPreviewList.builder()
                .feeds(feedDtos)
                .nextCursor(newCursor)
                .build();
    }


    @Override
    public FeedResponseDto.FeedPreviewList searchMyFeedsByKeyword(Long nextCursor, int pageSize, String keyword) {
        User user = authService.getCurrentUser();

        Pageable pageable = PageRequest.of(0, pageSize);
        List<Feed> feeds = feedRepository.searchFeedsByUserAndKeyword(user.getId(), keyword, nextCursor, pageable);

        List<Long> likedIds = feedLikeRepository.findFeedIdsByUserId(user.getId());
        List<Long> savedIds = feedSaveRepository.findFeedIdsByUserId(user.getId());

        List<FeedResponseDto.FeedDto> feedDtos = feeds.stream()
                .map(feed -> {
                    boolean isLiked = likedIds.contains(feed.getFeedId());
                    boolean isSaved = savedIds.contains(feed.getFeedId());
                    boolean isOwner = feed.getUser().getId().equals(user.getId());
                    long saveCount = feedSaveRepository.countByFeedId(feed.getFeedId());

                    return feedConverter.feedDto(feed, isOwner, isLiked, isSaved, saveCount);
                })
                .collect(Collectors.toList());

        Long newCursor = feeds.isEmpty() ? null : feeds.get(feeds.size() - 1).getFeedId();


        return FeedResponseDto.FeedPreviewList.builder()
                .feeds(feedDtos)
                .nextCursor(newCursor)
                .build();
    }

    @Transactional
    @Override
    public FeedResponseDto.FeedPreviewList searchByUserIdByKeyword(Long userId, Long nextCursor, int pageSize, String keyword) {
        // 현재 로그인한 사용자
        User loginUser = authService.getCurrentUser();
        Long loginUserId = loginUser.getId();

        // 로그인한 사용자의 좋아요/저장 목록 조회
        List<Long> likedFeedIds = feedLikeRepository.findFeedIdsByUserId(loginUserId);
        List<Long> savedFeedIds = feedSaveRepository.findFeedIdsByUserId(loginUserId);

        // 커서 기반 피드 조회
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "feedId"));
        List<Feed> feeds = (nextCursor == null)
                ? feedRepository.findByUser_Id(userId, pageable)
                : feedRepository.findByUserIdAndFeedIdBefore(userId, nextCursor, pageable);

        // 키워드 필터링
        String lowerKeyword = (keyword != null) ? keyword.toLowerCase() : null;
        List<Feed> filteredFeeds = feeds.stream()
                .filter(feed -> {
                    if (lowerKeyword == null || lowerKeyword.isBlank()) return true;
                    return (feed.getTitle() != null && feed.getTitle().toLowerCase().contains(lowerKeyword)) ||
                            (feed.getContent() != null && feed.getContent().toLowerCase().contains(lowerKeyword));
                })
                .collect(Collectors.toList());

        // DTO 변환
        List<FeedResponseDto.FeedDto> feedDtos = filteredFeeds.stream()
                .map(feed -> {
                    boolean isOwner = Objects.equals(loginUserId, feed.getUser().getId());
                    boolean isLiked = likedFeedIds.contains(feed.getFeedId());
                    boolean isSaved = savedFeedIds.contains(feed.getFeedId());
                    int saveCount = feedSaveRepository.countByFeed(feed);

                    return feedConverter.feedDto(feed, isOwner, isLiked, isSaved, saveCount);
                })
                .collect(Collectors.toList());

        Long nextCursorResult = (!filteredFeeds.isEmpty() && filteredFeeds.size() >= pageSize)
                ? filteredFeeds.get(filteredFeeds.size() - 1).getFeedId()
                : null;

        return FeedResponseDto.FeedPreviewList.builder()
                .feeds(feedDtos)
                .nextCursor(nextCursorResult)
                .build();
    }
}