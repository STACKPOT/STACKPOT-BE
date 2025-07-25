package stackpot.stackpot.feed.service;

import stackpot.stackpot.feed.entity.Feed;
import stackpot.stackpot.feed.dto.FeedResponseDto;

import java.util.Map;

public interface FeedQueryService {
     FeedResponseDto.FeedPreviewList getPreViewFeeds(String category, String sort, Long cursor, int limit);
     FeedResponseDto.AuthorizedFeedDto getFeed(Long feedId);
     FeedResponseDto.FeedPreviewList searchByUserIdByKeyword(Long userId, Long nextCursor, int pageSize);
     FeedResponseDto.FeedPreviewList getFeeds(Long nextCursor, int pageSize);
     Map<Long, String> getMySeries();
     Long getLikeCount(Long feedId);
     Feed getFeedByFeedId(Long feedId);
     Map<String, Object> getLikedFeedsWithPaging(int page, int size);
     FeedResponseDto.FeedPreviewList searchMyFeeds(Long nextCursor, int pageSize, String keyword);

     FeedResponseDto.FeedPreviewList searchMyFeedsByKeyword(Long nextCursor, int pageSize, String keyword);


     FeedResponseDto.FeedPreviewList searchByUserIdByKeyword(Long userId, Long nextCursor, int pageSize, String keyword);
}
