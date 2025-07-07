package stackpot.stackpot.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.feed.converter.FeedConverter;
import stackpot.stackpot.feed.dto.FeedResponseDto;
import stackpot.stackpot.pot.converter.PotConverter;
import stackpot.stackpot.feed.entity.Feed;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.repository.PotSaveRepository;
import stackpot.stackpot.save.converter.FeedSaveRepository;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.feed.repository.FeedLikeRepository;
import stackpot.stackpot.feed.repository.FeedRepository;
import stackpot.stackpot.pot.repository.PotRepository;
import stackpot.stackpot.user.repository.UserRepository;
import stackpot.stackpot.feed.dto.FeedSearchResponseDto;
import stackpot.stackpot.pot.dto.PotPreviewResponseDto;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final PotRepository potRepository;
    private final FeedRepository feedRepository;
    private final PotConverter potConverter;
    private final FeedConverter feedConverter;
    private final FeedLikeRepository feedLikeRepository;
    private final UserRepository userRepository;
    private final PotSaveRepository potSaveRepository;
    private final AuthService authService;
    private final FeedSaveRepository feedSaveRepository;


    @Override
    @Transactional(readOnly = true)
    public Page<PotPreviewResponseDto> searchPots(String keyword, Pageable pageable) {
        Page<Pot> pots = potRepository.searchByKeyword(keyword, pageable);

        // Pot ID 리스트 추출
        List<Pot> potList = pots.getContent();
        List<Long> potIds = potList.stream()
                .map(Pot::getPotId)
                .collect(Collectors.toList());

        // 현재 로그인 사용자 가져오기 (비로그인 허용 시 예외 처리 필요)
        User user = null;
        try {
            user = authService.getCurrentUser();
        } catch (Exception e) {
            user = null; // 비로그인 사용자
        }

        // 저장 수 및 유저의 저장 여부 일괄 조회
        Map<Long, Integer> potSaveCountMap = potSaveRepository.countSavesByPotIds(potIds);
        Set<Long> savedPotIds = (user != null)
                ? potSaveRepository.findPotIdsByUserIdAndPotIds(user.getId(), potIds)
                : Collections.emptySet();

        // 변환
        return pots.map(pot -> {
            User owner = pot.getUser();
            List<String> recruitmentRoles = pot.getRecruitmentDetails().stream()
                    .map(rd -> rd.getRecruitmentRole().name())
                    .collect(Collectors.toList());

            boolean isSaved = savedPotIds.contains(pot.getPotId());
            int saveCount = potSaveCountMap.getOrDefault(pot.getPotId(), 0);

            return potConverter.toPrviewDto(owner, pot, recruitmentRoles, isSaved, saveCount);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeedResponseDto.FeedDto> searchFeeds(String keyword, Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null &&
                !(authentication instanceof AnonymousAuthenticationToken) &&
                authentication.isAuthenticated();

        User user;
        Long userId = null;
        if (isAuthenticated) {
            user = authService.getCurrentUser();
            userId = user.getId();
        } else {
            user = null;
        }

        // 키워드 기반 검색
        Page<Feed> feeds = feedRepository.findByTitleContainingOrContentContainingOrderByCreatedAtDesc(
                keyword, keyword, pageable
        );

        // 좋아요/저장 정보 조회
        List<Long> likedFeedIds = (userId != null)
                ? feedLikeRepository.findFeedIdsByUserId(userId)
                : Collections.emptyList();

        List<Long> savedFeedIds = (userId != null)
                ? feedSaveRepository.findFeedIdsByUserId(userId)
                : Collections.emptyList();

        // 각 Feed를 FeedDto로 변환
        return feeds.map(feed -> {
            boolean isOwner = user != null && Objects.equals(user.getId(), feed.getUser().getId());
            Boolean isLiked = likedFeedIds.contains(feed.getFeedId());
            Boolean isSaved = savedFeedIds.contains(feed.getFeedId());
            int saveCount = feedSaveRepository.countByFeed(feed);

            return feedConverter.feedDto(feed, isOwner, isLiked, isSaved, saveCount);
        });
    }

}

