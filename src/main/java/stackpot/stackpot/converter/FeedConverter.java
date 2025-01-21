package stackpot.stackpot.converter;


import stackpot.stackpot.domain.Feed;
import stackpot.stackpot.web.dto.FeedResponseDto;

import java.util.List;

public interface FeedConverter {
    FeedResponseDto.FeedPreViewDto feedPreViewDto(Feed feed);

    FeedResponseDto.FeedPreViewListDto feedPreViewListDTO(List<Feed> feedList);
}
