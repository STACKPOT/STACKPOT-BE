package stackpot.stackpot.save.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.FeedHandler;
import stackpot.stackpot.apiPayload.exception.handler.PotHandler;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.feed.converter.FeedConverter;
import stackpot.stackpot.feed.dto.FeedResponseDto;
import stackpot.stackpot.feed.entity.Feed;
import stackpot.stackpot.feed.entity.mapping.FeedSave;
import stackpot.stackpot.feed.repository.FeedLikeRepository;
import stackpot.stackpot.feed.repository.FeedRepository;
import stackpot.stackpot.pot.converter.PotConverter;
import stackpot.stackpot.pot.dto.PotPreviewResponseDto;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.entity.mapping.PotSave;
import stackpot.stackpot.pot.repository.PotMemberRepository;
import stackpot.stackpot.pot.repository.PotRepository;
import stackpot.stackpot.pot.repository.PotSaveRepository;
import stackpot.stackpot.save.repository.FeedSaveRepository;
import stackpot.stackpot.user.entity.User;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaveServiceImpl implements SaveService {
    private final AuthService authService;
    private final FeedRepository feedRepository;
    private final FeedSaveRepository feedSaveRepository;
    private final PotSaveRepository potSaveRepository;
    private final PotRepository potRepository;
    private final PotConverter potConverter;
    private final FeedLikeRepository feedLikeRepository;
    private final FeedConverter feedConverter;
    private final PotMemberRepository potMemberRepository;

    @Override
    @Transactional
    public String feedSave(Long feedId) {
        User currentUser = authService.getCurrentUser();
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedHandler(ErrorStatus.FEED_NOT_FOUND));

        Optional<FeedSave> existingSave = feedSaveRepository.findByFeedAndUser(feed, currentUser);

        if (existingSave.isPresent()) {
            feedSaveRepository.delete(existingSave.get());
            return "저장 취소했습니다";
        } else {
            FeedSave feedSave = FeedSave.builder()
                    .feed(feed)
                    .user(currentUser)
                    .build();
            feedSaveRepository.save(feedSave);
            return "저장했습니다";
        }
    }

    @Transactional
    @Override
    public String potSave(Long potId) {
        User currentUser = authService.getCurrentUser();
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        Optional<PotSave> existingSave = potSaveRepository.findByUserAndPot(currentUser, pot);

        if (existingSave.isPresent()) {
            potSaveRepository.delete(existingSave.get());
            return "저장 취소했습니다";
        } else {
            PotSave save = PotSave.builder()
                    .user(currentUser)
                    .pot(pot)
                    .build();
            potSaveRepository.save(save);
            return "저장했습니다";
        }
    }

    @Override
    public Map<String, Object> getSavedPotsWithPaging(int page, int size) {
        User user = authService.getCurrentUser(); // 인증 필요

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Pot> potPage = potSaveRepository.findSavedPotsByUserId(user.getId(), pageable);

        List<Pot> pots = potPage.getContent();
        List<Long> potIds = pots.stream()
                .map(Pot::getPotId)
                .collect(Collectors.toList());

        Map<Long, Integer> potSaveCountMap = potSaveRepository.countSavesByPotIds(potIds);

        Set<Long> memberPotIds = (user != null && !potIds.isEmpty())
                ? potMemberRepository.findPotIdsByUserIdAndPotIds(user.getId(), potIds)
                : Collections.emptySet();

        List<PotPreviewResponseDto> content = pots.stream()
                .map(pot -> {
                    Long potId = pot.getPotId();
                    List<String> roles = pot.getRecruitmentDetails().stream()
                            .map(rd -> String.valueOf(rd.getRecruitmentRole()))
                            .collect(Collectors.toList());

                    boolean isSaved = true;
                    boolean isMember = memberPotIds.contains(potId);
                    int saveCount = potSaveCountMap.getOrDefault(potId, 0);

                    return potConverter.toPrviewDto(pot.getUser(), pot, roles, isSaved, saveCount, isMember);
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("pots", content);
        response.put("currentPage", potPage.getNumber() + 1);
        response.put("totalPages", potPage.getTotalPages());
        response.put("totalElements", potPage.getTotalElements());
        response.put("size", potPage.getSize());

        return response;
    }

    @Override
    public Map<String, Object> getSavedFeedsWithPaging(int page, int size) {
        User user = authService.getCurrentUser(); // 인증 필요

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Feed> feedPage = feedSaveRepository.findSavedFeedsByUserId(user.getId(), pageable); // 저장된 피드들 조회

        List<Feed> feeds = feedPage.getContent();
        List<Long> feedIds = feeds.stream()
                .map(Feed::getFeedId)
                .collect(Collectors.toList());

        // 미리 좋아요한 피드 ID 조회
        List<Long> likedFeedIds = feedLikeRepository.findFeedIdsByUserId(user.getId());

        // 저장 수 조회
        List<Object[]> rawResults = feedSaveRepository.countSavesByFeedIds(feedIds);
        Map<Long, Integer> saveCountMap = rawResults.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Long) row[1]).intValue()
                ));

        // DTO 변환
        List<FeedResponseDto.FeedDto> content = feeds.stream()
                .map(feed -> {
                    Long feedId = feed.getFeedId();
                    boolean isSaved = true;
                    boolean isLiked = likedFeedIds.contains(feedId);
                    boolean isOwner = feed.getUser().getId().equals(user.getId());
                    int saveCount = saveCountMap.getOrDefault(feedId, 0);

                    return feedConverter.feedDto(feed, isOwner, isLiked, isSaved, saveCount);
                })
                .collect(Collectors.toList());

        // 결과 Map 생성
        Map<String, Object> response = new HashMap<>();
        response.put("feeds", content);
        response.put("currentPage", feedPage.getNumber() + 1);
        response.put("totalPages", feedPage.getTotalPages());
        response.put("totalElements", feedPage.getTotalElements());
        response.put("size", feedPage.getSize());

        return response;
    }
}
