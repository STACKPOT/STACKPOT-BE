package stackpot.stackpot.service;

import stackpot.stackpot.web.dto.FeedResponseDto;

public interface FeedService {
    public FeedResponseDto.FeedResponse getPreViewFeeds(String category, String sort, String cursor, int limit);
}
