package stackpot.stackpot.feed.converter;


import stackpot.stackpot.feed.entity.Feed;
import stackpot.stackpot.feed.dto.FeedRequestDto;
import stackpot.stackpot.feed.dto.FeedResponseDto;
import stackpot.stackpot.feed.dto.FeedSearchResponseDto;

public interface FeedConverter {
    FeedResponseDto.FeedDto feedDto(Feed feed);
    Feed toFeed(FeedRequestDto.createDto request);
    FeedSearchResponseDto toSearchDto(Feed feed);
    FeedResponseDto.FeedDto toAuthorizedFeedDto(Feed feed, boolean isOwner);
}
