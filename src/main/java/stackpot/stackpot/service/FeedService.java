package stackpot.stackpot.service;

import stackpot.stackpot.domain.Feed;
import stackpot.stackpot.domain.enums.Category;
import stackpot.stackpot.web.dto.FeedRequestDto;
import stackpot.stackpot.web.dto.FeedResponseDto;

public interface FeedService {
     FeedResponseDto.FeedPreviewList getPreViewFeeds(Category category, String sort, String cursor, int limit);
     Feed createFeed(FeedRequestDto.createDto request);


     Feed getFeed(Long feedId);
     FeedResponseDto.FeedPreviewList getFeedsByUserId(Long userId, String nextCursor, int pageSize);
     FeedResponseDto.FeedPreviewList getFeeds(String nextCursor, int pageSize);


     Feed modifyFeed(long feedId, FeedRequestDto.createDto request);
     boolean toggleLike(Long feedId);
     boolean toggleSave(Long feedId);

     Long getSaveCount(Long feedId);
     Long getLikeCount(Long feedId);
}
