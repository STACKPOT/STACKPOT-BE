package stackpot.stackpot.user.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import stackpot.stackpot.user.repository.TempUserRepository;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TempUserCleanupScheduler {

    private final TempUserRepository tempUserRepository;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional// 매 정시
    public void deleteOldTempUsers() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        int deletedCount = tempUserRepository.deleteByCreatedAtBefore(oneHourAgo);
        log.info("1시간 지난 TempUser {}개 삭제 완료", deletedCount);
    }
}