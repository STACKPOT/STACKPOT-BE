package stackpot.stackpot.feed.service;

import stackpot.stackpot.feed.dto.SeriesRequestDto;
import stackpot.stackpot.feed.entity.Feed;
import stackpot.stackpot.feed.dto.FeedRequestDto;
import stackpot.stackpot.feed.dto.FeedResponseDto;

import java.util.List;
import java.util.Map;

public interface FeedService {
     FeedResponseDto.FeedPreviewList getPreViewFeeds(String category, String sort, Long cursor, int limit);
     FeedResponseDto.CreatedFeedDto createFeed(FeedRequestDto.createDto request);

     FeedResponseDto.AuthorizedFeedDto getFeed(Long feedId);
     FeedResponseDto.FeedPreviewList getFeedsByUserId(Long userId, Long nextCursor, int pageSize);
     FeedResponseDto.FeedPreviewList getFeeds(Long nextCursor, int pageSize);

     FeedResponseDto.CreatedFeedDto modifyFeed(long feedId, FeedRequestDto.createDto request);

     String deleteFeed(Long feedId);

     boolean toggleLike(Long feedId);

     Long getLikeCount(Long feedId);

     Feed getFeedByFeedId(Long feedId);

     Map<Long, String> createSeries(SeriesRequestDto requestDto);

     Map<Long, String> getMySeries();
}
