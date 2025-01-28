package stackpot.stackpot.service;

import stackpot.stackpot.domain.Feed;
import stackpot.stackpot.domain.enums.Category;
import stackpot.stackpot.web.dto.FeedRequestDto;
import stackpot.stackpot.web.dto.FeedResponseDto;

public interface FeedService {
    public FeedResponseDto.FeedPreviewList getPreViewFeeds(Category category, String sort, String cursor, int limit);
    public Feed createFeed(FeedRequestDto.createDto request);

    public Feed getFeed(Long feedId);
    public FeedResponseDto.FeedPreviewList getFeedsByUserId(Long userId, String nextCursor, int pageSize);
    public FeedResponseDto.FeedPreviewList getFeeds(String nextCursor, int pageSize);


    public Feed modifyFeed(long feedId, FeedRequestDto.createDto request);
    public boolean toggleLike(Long feedId);
    public boolean toggleSave(Long feedId);

    public Long getSaveCount(Long feedId);
    public Long getLikeCount(Long feedId);
}
