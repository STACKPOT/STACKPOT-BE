package stackpot.stackpot.converter;

import stackpot.stackpot.domain.Feed;
import stackpot.stackpot.web.dto.FeedResponseDto;

import java.util.List;

public class FeedConverterImpl implements FeedConverter{
    @Override
    public FeedResponseDto.FeedPreViewDto feedPreViewDto(Feed feed) {
        return null;
    }

    @Override
    public FeedResponseDto.FeedPreViewListDto feedPreViewListDTO(List<Feed> feedList) {
        return null;
    }
}
