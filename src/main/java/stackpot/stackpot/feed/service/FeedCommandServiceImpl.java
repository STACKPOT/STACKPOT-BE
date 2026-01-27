package stackpot.stackpot.feed.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.FeedHandler;
import stackpot.stackpot.apiPayload.exception.handler.UserHandler;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.common.util.RedisUtil;
import stackpot.stackpot.feed.converter.FeedConverter;
import stackpot.stackpot.feed.dto.FeedCacheDto;
import stackpot.stackpot.feed.dto.FeedRequestDto;
import stackpot.stackpot.feed.dto.FeedResponseDto;
import stackpot.stackpot.feed.dto.SeriesRequestDto;
import stackpot.stackpot.feed.entity.Feed;
import stackpot.stackpot.feed.entity.Series;
import stackpot.stackpot.feed.entity.mapping.FeedLike;
import stackpot.stackpot.feed.repository.FeedLikeRepository;
import stackpot.stackpot.feed.repository.FeedRepository;
import stackpot.stackpot.save.repository.FeedSaveRepository;
import stackpot.stackpot.feed.repository.SeriesRepository;
import stackpot.stackpot.notification.dto.NotificationResponseDto;
import stackpot.stackpot.notification.event.FeedLikeEvent;
import stackpot.stackpot.notification.repository.FeedLikeNotificationRepository;
import stackpot.stackpot.notification.service.NotificationCommandService;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.repository.UserRepository;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedCommandServiceImpl implements FeedCommandService {

    private final NotificationCommandService notificationCommandService;
    private final FeedRepository feedRepository;
    private final FeedConverter feedConverter;
    private final UserRepository userRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final SeriesRepository seriesRepository;
    private final AuthService authService;
    private final FeedSaveRepository feedSaveRepository;
    private final RedisUtil redisUtil;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RedisTemplate<String, String> redisTemplate;
    private final FeedLikeNotificationRepository feedLikeNotificationRepository;

    @Override
    public FeedResponseDto.CreatedFeedDto createFeed(FeedRequestDto.createDto request) {
        User user = authService.getCurrentUser();

        Series series = null;
        if (request.getSeriesId() != null) {
            series = seriesRepository.findById(request.getSeriesId())
                    .orElseThrow(() -> new FeedHandler(ErrorStatus.SERIES_NOT_FOUND));
        }

        Feed feed = feedConverter.toFeed(request, series);
        feed.setUser(user);

        Feed saved = feedRepository.save(feed);

        return feedConverter.createFeedDto(saved);
    }




    @Transactional
    public FeedResponseDto.FeedPreviewList getFeedsByUserId(Long userId, Long nextCursor, int pageSize) {
        // 현재 로그인한 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.isAuthenticated();

        final User loginUser = isAuthenticated
                ? userRepository.findByUserId(authService.getCurrentUserId())
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND))
                : null;

        final Long loginUserId = loginUser != null ? loginUser.getId() : null;

        // 좋아요, 저장 목록 미리 조회
        final List<Long> likedFeedIds = (loginUserId != null)
                ? feedLikeRepository.findFeedIdsByUserId(loginUserId)
                : List.of();

        final List<Long> savedFeedIds = (loginUserId != null)
                ? feedSaveRepository.findFeedIdsByUserId(loginUserId)
                : List.of();

        // 피드 조회 (페이징)
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "feedId"));
        List<Feed> feeds = (nextCursor == null)
                ? feedRepository.findByUser_Id(userId, pageable)
                : feedRepository.findByUserIdAndFeedIdBefore(userId, nextCursor, pageable);

        // Feed -> FeedDto 변환
        List<FeedResponseDto.FeedDto> feedDtos = feeds.stream()
                .map(feed -> {
                    boolean isOwner = loginUserId != null && feed.getUser().getId().equals(loginUserId);
                    Boolean isLiked = loginUserId != null && likedFeedIds.contains(feed.getFeedId());
                    Boolean isSaved = loginUserId != null && savedFeedIds.contains(feed.getFeedId());
                    int saveCount = feedSaveRepository.countByFeed(feed);

                    return feedConverter.feedDto(feed, isOwner, isLiked, isSaved, saveCount);
                })
                .collect(Collectors.toList());

        // 다음 커서 설정
        Long nextCursorResult = (!feeds.isEmpty() && feeds.size() >= pageSize)
                ? feeds.get(feeds.size() - 1).getFeedId()
                : null;

        return FeedResponseDto.FeedPreviewList.builder()
                .feeds(feedDtos)
                .nextCursor(nextCursorResult)
                .build();
    }

    @Override
    public FeedResponseDto.CreatedFeedDto modifyFeed(long feedId, FeedRequestDto.createDto request) {
        User user = authService.getCurrentUser();

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedHandler(ErrorStatus.FEED_NOT_FOUND));

        if (!feed.getUser().getEmail().equals(user.getEmail())) {
            throw new FeedHandler(ErrorStatus.FEED_UNAUTHORIZED);
        }

        if (request.getTitle() != null) {
            feed.setTitle(request.getTitle());
        }

        if (request.getContent() != null) {
            feed.setContent(request.getContent());
        }

        if (request.getCategories() != null) {
            feed.setCategories(request.getCategories());
        }

        if (request.getInterests() != null) {
            feed.setInterests(request.getInterests());
        }

        if (request.getSeriesId() != null) {
            Series series = seriesRepository.findById(request.getSeriesId())
                    .orElseThrow(() -> new FeedHandler(ErrorStatus.SERIES_NOT_FOUND));
            feed.setSeries(series);
        } else {
            feed.setSeries(null); // 시리즈 제거
        }

        Feed updated = feedRepository.save(feed);
        return feedConverter.createFeedDto(updated);
    }

    @Override
    public String deleteFeed(Long feedId) {
        User user = authService.getCurrentUser();

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedHandler(ErrorStatus.FEED_NOT_FOUND));

        if (!feed.getUser().getEmail().equals(user.getEmail())) {
            throw new FeedHandler(ErrorStatus.FEED_UNAUTHORIZED);
        }

        feedRepository.delete(feed);

        return "피드를 삭제했습니다.";
    }

    @Transactional
    @Override
    public boolean toggleLike(Long feedId) {
        User user = authService.getCurrentUser();

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedHandler(ErrorStatus.FEED_NOT_FOUND));
        Optional<FeedLike> existingLike = feedLikeRepository.findByFeedAndUser(feed, user);

        if (existingLike.isPresent()) {
            // 이미 좋아요가 있다면 삭제 (좋아요 취소)
            FeedLike feedLike = existingLike.get();
            // 연관된 알림 먼저 삭제
            feedLikeNotificationRepository.deleteByFeedLikeId(feedLike.getLikeId());
            // 좋아요 삭제
            feedLikeRepository.delete(feedLike);
            feed.setLikeCount(feed.getLikeCount() - 1);
            feedRepository.save(feed);
            return false; // 좋아요 취소
        } else {
            // 좋아요 추가
            FeedLike feedLike = FeedLike.builder()
                    .feed(feed)
                    .user(user)
                    .build();
            FeedLike savedFeedLike = feedLikeRepository.save(feedLike);

            NotificationResponseDto.UnReadNotificationDto dto = notificationCommandService.createFeedLikeNotification(
                    feed.getFeedId(), savedFeedLike.getLikeId(), user.getId());

            applicationEventPublisher.publishEvent(new FeedLikeEvent(feed.getUser().getUserId(), dto));

            feed.setLikeCount(feed.getLikeCount() + 1);
            feedRepository.save(feed);
            return true; // 좋아요 성공
        }
    }

    @Override
    @Transactional
    public Map<Long, String> createSeries(SeriesRequestDto requestDto) {
        User user = authService.getCurrentUser();

        List<Series> existingSeries = seriesRepository.findAllByUser(user);
        @NotNull Set<String> existingComments = existingSeries.stream()
                .map(Series::getComment)
                .collect(Collectors.toSet());

        Set<String> incomingComments = new HashSet<>(requestDto.getComments());

        // 최대 5개 제약
        if (incomingComments.size() > 5) {
            throw new FeedHandler(ErrorStatus.SERIES_BAD_REQUEST);
        }

        //  삭제: 기존에 있었는데 지금은 없음
        List<Series> toDelete = existingSeries.stream()
                .filter(series -> !incomingComments.contains(series.getComment()))
                .toList();

        for (Series series : toDelete) {
            feedRepository.clearSeriesReference(series.getSeriesId()); // feed의 series_id null 처리
        }
        seriesRepository.deleteAll(toDelete);

        //  생성: 지금 있는데 기존엔 없었던 것
        List<String> toCreate = incomingComments.stream()
                .filter(comment -> !existingComments.contains(comment))
                .toList();

        List<Series> newSeries = toCreate.stream()
                .map(comment -> feedConverter.toEntity(comment, user))
                .toList();

        seriesRepository.saveAll(newSeries);

        //  전체 목록 다시 조회
        List<Series> updatedSeries = seriesRepository.findAllByUser(user);
        return updatedSeries.stream()
                .collect(Collectors.toMap(
                        Series::getSeriesId,
                        Series::getComment
                ));
    }

}