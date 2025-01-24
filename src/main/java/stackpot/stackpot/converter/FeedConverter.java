package stackpot.stackpot.converter;


import stackpot.stackpot.domain.Feed;
import stackpot.stackpot.web.dto.FeedRequestDto;
import stackpot.stackpot.web.dto.FeedResponseDto;

import java.util.List;

public interface FeedConverter {
    FeedResponseDto.FeedDto feedDto(Feed feed, long popularity, long likeCount);
    Feed toFeed(FeedRequestDto.createDto request);

}
