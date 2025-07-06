package stackpot.stackpot.save.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.FeedHandler;
import stackpot.stackpot.apiPayload.exception.handler.PotHandler;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.feed.entity.Feed;
import stackpot.stackpot.feed.entity.mapping.FeedSave;
import stackpot.stackpot.feed.repository.FeedRepository;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.entity.mapping.PotSave;
import stackpot.stackpot.pot.repository.PotRepository;
import stackpot.stackpot.pot.repository.PotSaveRepository;
import stackpot.stackpot.save.converter.FeedSaveRepository;
import stackpot.stackpot.user.entity.User;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaveServiceImpl implements SaveService {
    private final AuthService authService;
    private final FeedRepository feedRepository;
    private final FeedSaveRepository feedSaveRepository;
    private final PotSaveRepository potSaveRepository;
    private final PotRepository potRepository;



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
}
