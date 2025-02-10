package stackpot.stackpot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stackpot.stackpot.converter.FeedConverter;
import stackpot.stackpot.converter.PotConverter;
import stackpot.stackpot.domain.Feed;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.repository.FeedRepository.FeedRepository;
import stackpot.stackpot.repository.PotRepository.PotRepository;
import stackpot.stackpot.web.dto.FeedSearchResponseDto;
import stackpot.stackpot.web.dto.PotPreviewResponseDto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final PotRepository potRepository;
    private final FeedRepository feedRepository;
    private final PotConverter potConverter;
    private final FeedConverter feedConverter;

//    @Override
//    @Transactional(readOnly = true)
//    public Page<PotSearchResponseDto> searchPots(String keyword, Pageable pageable) {
//        Page<Pot> pots = potRepository.searchByKeyword(keyword, pageable);
//        return pots.map(potConverter::toSearchDto);
//    }
@Override
@Transactional(readOnly = true)
public List<PotPreviewResponseDto> searchPots(String keyword, Pageable pageable) {
    Page<Pot> pots = potRepository.searchByKeyword(keyword, pageable);

    if (pots.isEmpty()) {
        return Collections.emptyList();
    }

    return pots.stream().map(pot -> {
        User user = pot.getUser();
        List<String> recruitmentRoles = pot.getRecruitmentDetails().stream()
                .map(rd -> rd.getRecruitmentRole().name())
                .collect(Collectors.toList());

        return potConverter.toPrviewDto(user, pot, recruitmentRoles);
    }).collect(Collectors.toList());
}


    @Override
    @Transactional(readOnly = true)
    public List<FeedSearchResponseDto> searchFeeds(String keyword, Pageable pageable) {
        Page<Feed> feeds = feedRepository.findByTitleContainingOrContentContainingOrderByCreatedAtDesc(keyword, keyword, pageable);

        if (feeds.isEmpty()) {
            return Collections.emptyList();
        }

        // Page → Stream 변환 후 List로 변환
        return feeds.getContent().stream()
                .map(feedConverter::toSearchDto)
                .collect(Collectors.toList());
    }


}

