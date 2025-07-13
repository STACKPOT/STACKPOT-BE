package stackpot.stackpot.search.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import stackpot.stackpot.feed.dto.FeedResponseDto;
import stackpot.stackpot.feed.dto.FeedSearchResponseDto;
import stackpot.stackpot.pot.dto.PotPreviewResponseDto;

public interface SearchService {
    Page<PotPreviewResponseDto> searchPots(String keyword, Pageable pageable);
    Page<FeedResponseDto.FeedDto> searchFeeds(String keyword, Pageable pageable);
}
