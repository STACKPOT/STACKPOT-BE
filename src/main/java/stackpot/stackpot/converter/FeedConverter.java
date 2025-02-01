package stackpot.stackpot.converter;


import stackpot.stackpot.domain.Feed;
import stackpot.stackpot.web.dto.FeedRequestDto;
import stackpot.stackpot.web.dto.FeedResponseDto;
import stackpot.stackpot.web.dto.FeedSearchResponseDto;

public interface FeedConverter {
    FeedResponseDto.FeedDto feedDto(Feed feed);
    Feed toFeed(FeedRequestDto.createDto request);
    FeedSearchResponseDto toSearchDto(Feed feed);
}
