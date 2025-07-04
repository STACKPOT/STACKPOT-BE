package stackpot.stackpot.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.feed.converter.FeedConverter;
import stackpot.stackpot.pot.converter.PotConverter;
import stackpot.stackpot.feed.entity.Feed;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.repository.PotSaveRepository;
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
    public Page<FeedSearchResponseDto> searchFeeds(String keyword, Pageable pageable) {


        // 현재 로그인한 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated();
        Long userId = isAuthenticated ? ((User) authentication.getPrincipal()).getId() : null;
        Page<Feed> feeds = feedRepository.findByTitleContainingOrContentContainingOrderByCreatedAtDesc(keyword, keyword, pageable);


        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));


        // 좋아요를 눌렀던 피드 ID 리스트 가져오기
        List<Long> likedFeedIds = isAuthenticated && userId != null
                ? feedLikeRepository.findFeedIdsByUserId(userId) // 로그인한 사용자의 좋아요한 피드 ID 리스트
                : Collections.emptyList(); // 비로그인 상태에서는 빈 리스트

        // 각 Feed 객체에 대해 좋아요 여부를 확인하고 FeedDto로 변환
        return feeds.map(feed -> {
            boolean isOwner = (user != null) && Objects.equals(userId, feed.getUser().getUserId());

            Boolean isLiked = isAuthenticated && userId != null
                    ? likedFeedIds.contains(feed.getFeedId()) // 로그인한 사용자가 좋아요를 눌렀으면 true, 아니면 false
                    : null; // 비로그인 사용자는 null 처리

            FeedSearchResponseDto feedDto = feedConverter.toSearchDto(feed);
            feedDto.setIsLiked(isLiked); // 좋아요 상태 추가
            return feedDto;
        });
    }

}

