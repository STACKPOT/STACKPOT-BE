package stackpot.stackpot.feed.service;

import stackpot.stackpot.feed.dto.FeedRequestDto;
import stackpot.stackpot.feed.dto.FeedResponseDto;
import stackpot.stackpot.feed.dto.SeriesRequestDto;

import java.util.Map;

public interface FeedCommandService {
     FeedResponseDto.CreatedFeedDto createFeed(FeedRequestDto.createDto request);

     FeedResponseDto.CreatedFeedDto modifyFeed(long feedId, FeedRequestDto.createDto request);

     String deleteFeed(Long feedId);

     boolean toggleLike(Long feedId);

     Map<Long, String> createSeries(SeriesRequestDto requestDto);
}