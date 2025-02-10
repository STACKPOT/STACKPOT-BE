package stackpot.stackpot.service;

import org.springframework.data.domain.Pageable;
import stackpot.stackpot.web.dto.FeedSearchResponseDto;
import stackpot.stackpot.web.dto.PotPreviewResponseDto;

import java.util.List;

public interface SearchService {
    List<PotPreviewResponseDto> searchPots(String keyword, Pageable pageable);
    List<FeedSearchResponseDto> searchFeeds(String keyword, Pageable pageable);
}
