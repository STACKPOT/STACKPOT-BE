package stackpot.stackpot.service;

import stackpot.stackpot.domain.Feed;
import stackpot.stackpot.domain.enums.Category;
import stackpot.stackpot.web.dto.FeedRequestDto;
import stackpot.stackpot.web.dto.FeedResponseDto;

public interface FeedService {
     FeedResponseDto.FeedPreviewList getPreViewFeeds(String category, String sort, Long cursor, int limit);
     Feed createFeed(FeedRequestDto.createDto request);


     Feed getFeed(Long feedId);
     FeedResponseDto.FeedPreviewList getFeedsByUserId(Long userId, Long nextCursor, int pageSize);
     FeedResponseDto.FeedPreviewList getFeeds(Long nextCursor, int pageSize);

     Feed modifyFeed(long feedId, FeedRequestDto.createDto request);
     boolean toggleLike(Long feedId);

     Long getLikeCount(Long feedId);
}
