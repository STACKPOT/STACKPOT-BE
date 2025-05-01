package stackpot.stackpot.feed.service;

import stackpot.stackpot.feed.entity.Feed;
import stackpot.stackpot.feed.dto.FeedRequestDto;
import stackpot.stackpot.feed.dto.FeedResponseDto;

public interface FeedService {
     FeedResponseDto.FeedPreviewList getPreViewFeeds(String category, String sort, Long cursor, int limit);
     Feed createFeed(FeedRequestDto.createDto request);

     FeedResponseDto.FeedDto getFeed(Long feedId);
     FeedResponseDto.FeedPreviewList getFeedsByUserId(Long userId, Long nextCursor, int pageSize);
     FeedResponseDto.FeedPreviewList getFeeds(Long nextCursor, int pageSize);



     Feed modifyFeed(long feedId, FeedRequestDto.createDto request);

     String deleteFeed(Long feedId);

     boolean toggleLike(Long feedId);

     Long getLikeCount(Long feedId);
}
