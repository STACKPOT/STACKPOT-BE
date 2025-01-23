package stackpot.stackpot.service;

import stackpot.stackpot.domain.Feed;
import stackpot.stackpot.domain.enums.Category;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.web.dto.FeedRequestDto;
import stackpot.stackpot.web.dto.FeedResponseDto;

public interface FeedService {
    public FeedResponseDto.FeedResponse getPreViewFeeds(Category category, String sort, String cursor, int limit);
    public Feed createFeed(FeedRequestDto.createDto request);
    public boolean toggleLike(Long feedId);
    public boolean toggleSave(Long feedId);
}
