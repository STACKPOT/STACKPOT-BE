package stackpot.stackpot.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import stackpot.stackpot.web.dto.FeedSearchResponseDto;
import stackpot.stackpot.web.dto.PotSearchResponseDto;

public interface SearchService {
    Page<PotSearchResponseDto> searchPots(String keyword, Pageable pageable);
    Page<FeedSearchResponseDto> searchFeeds(String keyword, Pageable pageable);
}
