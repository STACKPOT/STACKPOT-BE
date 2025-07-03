package stackpot.stackpot.feed.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
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
import stackpot.stackpot.feed.dto.FeedRequestDto;
import stackpot.stackpot.feed.dto.FeedResponseDto;
import stackpot.stackpot.feed.dto.SeriesRequestDto;
import stackpot.stackpot.feed.entity.Feed;
import stackpot.stackpot.feed.entity.Series;
import stackpot.stackpot.feed.entity.enums.Category;
import stackpot.stackpot.feed.entity.mapping.FeedLike;
import stackpot.stackpot.feed.repository.FeedLikeRepository;
import stackpot.stackpot.feed.repository.FeedRepository;
import stackpot.stackpot.notification.dto.NotificationResponseDto;
import stackpot.stackpot.feed.repository.SeriesRepository;
import stackpot.stackpot.notification.event.FeedLikeEvent;
import stackpot.stackpot.notification.service.NotificationCommandService;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.repository.UserRepository;

import java.util.*;
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

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public FeedResponseDto.FeedPreviewList getPreViewFeeds(String categoryStr, String sort, Long cursor, int limit) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.isAuthenticated();

        log.info("isAuthenticated :{}", isAuthenticated);

        final User user = isAuthenticated
                ? userRepository.findByUserId(authService.getCurrentUserId()).orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND))
                : null;

        final Long userId = (user != null) ? user.getId() : null;
        final List<Long> likedFeedIds = (userId != null)
                ? feedLikeRepository.findFeedIdsByUserId(userId)
                : List.of(); // 비로그인 사용자는 빈 리스트

        Long lastFeedId = Long.MAX_VALUE;  // 기본적으로 가장 큰 ID부터 조회
        Long lastFeedLike = 0L;

        if (cursor != null) {
            lastFeedId = cursor;
            Feed lastdFeed = feedRepository.findById(lastFeedId)
                    .orElseThrow(() -> new FeedHandler(ErrorStatus.FEED_NOT_FOUND));

            lastFeedLike = lastdFeed.getLikeCount();
        } else if (sort.equals("old")) {
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

            if (nextfeedResults.size() == 0) {
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

        return feedConverter.toAuthorizedFeedDto(feed, isOwner);
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
        Long nextCursorResult = (!feeds.isEmpty() && feeds.size() >= pageSize) ? feeds.get(feeds.size() - 1).getFeedId() : null;

        return FeedResponseDto.FeedPreviewList.builder()
                .feeds(feedDtos)
                .nextCursor(nextCursorResult)
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
        Long nextCursorResult = (!feeds.isEmpty() && feeds.size() >= pageSize) ? feeds.get(feeds.size() - 1).getFeedId() : null;

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

}