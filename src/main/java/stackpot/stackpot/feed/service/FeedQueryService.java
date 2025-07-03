package stackpot.stackpot.feed.service;

import stackpot.stackpot.feed.dto.FeedRequestDto;
import stackpot.stackpot.feed.dto.FeedResponseDto;
import stackpot.stackpot.feed.dto.SeriesRequestDto;
import stackpot.stackpot.feed.entity.Feed;

import java.util.Map;

public interface FeedQueryService {

     FeedResponseDto.CreatedFeedDto createFeed(FeedRequestDto.createDto request);
     FeedResponseDto.CreatedFeedDto modifyFeed(long feedId, FeedRequestDto.createDto request);
     String deleteFeed(Long feedId);
     boolean toggleLike(Long feedId);
     Map<Long, String> createSeries(SeriesRequestDto requestDto);
}
