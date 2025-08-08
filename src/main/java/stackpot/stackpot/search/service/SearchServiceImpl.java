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
import stackpot.stackpot.pot.repository.PotMemberRepository;
import stackpot.stackpot.pot.repository.PotSaveRepository;
import stackpot.stackpot.save.repository.FeedSaveRepository;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.feed.repository.FeedLikeRepository;
import stackpot.stackpot.feed.repository.FeedRepository;
import stackpot.stackpot.pot.repository.PotRepository;
import stackpot.stackpot.user.repository.UserRepository;
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
    private final PotMemberRepository potMemberRepository;


    @Override
    @Transactional(readOnly = true)
    public Page<PotPreviewResponseDto> searchPots(String keyword, Pageable pageable) {
        Page<Pot> pots = potRepository.searchByKeyword(keyword, pageable);

        List<Pot> potList = pots.getContent();
        List<Long> potIds = potList.stream()
                .map(Pot::getPotId)
                .collect(Collectors.toList());

        Long userId = null;
        try {
            User user = authService.getCurrentUser();
            userId = user.getId();
        } catch (Exception ignored) { /* 비로그인 */ }

        // 배치 집계(이미 배치 쿼리 사용 중이라 OK)
        Map<Long, Integer> potSaveCountMap = potSaveRepository.countSavesByPotIds(potIds);

        // List -> Set (contains O(1))
        Set<Long> savedPotIds = (userId != null)
                ? new HashSet<>(potSaveRepository.findPotIdsByUserIdAndPotIds(userId, potIds))
                : Collections.emptySet();

        Set<Long> memberPotIds = (userId != null)
                ? new HashSet<>(potMemberRepository.findPotIdsByUserIdAndPotIds(userId, potIds))
                : Collections.emptySet();

        return pots.map(pot -> {
            User owner = pot.getUser();
            List<String> recruitmentRoles = pot.getRecruitmentDetails().stream()
                    .map(rd -> rd.getRecruitmentRole().name())
                    .collect(Collectors.toList());

            boolean isSaved = savedPotIds.contains(pot.getPotId());
            int saveCount = potSaveCountMap.getOrDefault(pot.getPotId(), 0);
            boolean isMember = memberPotIds.contains(pot.getPotId());

            return potConverter.toPrviewDto(owner, pot, recruitmentRoles, isSaved, saveCount, isMember);
        });
    }


    @Override
    @Transactional(readOnly = true)
    public Page<FeedResponseDto.FeedDto> searchFeeds(String keyword, Pageable pageable) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.isAuthenticated();

        User user = isAuthenticated ? authService.getCurrentUser() : null;
        Long userId = (user != null) ? user.getId() : null;


        Page<Feed> feeds = feedRepository.findByTitleContainingOrContentContainingOrderByCreatedAtDesc(
                keyword, keyword, pageable
        );

        // === N+1 제거: 한 번에 집계/조회 ===
        List<Long> feedIds = feeds.getContent().stream()
                .map(Feed::getFeedId)
                .collect(Collectors.toList());

        // 저장 수 배치 집계
        Map<Long, Long> saveCountMap = feedSaveRepository.countByFeedIds(feedIds);

        // List -> Set (contains O(1))
        Set<Long> likedFeedIds = (userId != null)
                ? new HashSet<>(feedLikeRepository.findFeedIdsByUserId(userId))
                : Collections.emptySet();

        Set<Long> savedFeedIds = (userId != null)
                ? new HashSet<>(feedSaveRepository.findFeedIdsByUserId(userId))
                : Collections.emptySet();

        return feeds.map(feed -> {
            boolean isOwner = user != null && Objects.equals(user.getId(), feed.getUser().getId());
            Boolean isLiked = (userId != null) ? likedFeedIds.contains(feed.getFeedId()) : null;
            Boolean isSaved = (userId != null) ? savedFeedIds.contains(feed.getFeedId()) : null;
            int saveCount = saveCountMap.getOrDefault(feed.getFeedId(), 0L).intValue();

            return feedConverter.feedDto(feed, isOwner, isLiked, isSaved, saveCount);
        });
    }


}

