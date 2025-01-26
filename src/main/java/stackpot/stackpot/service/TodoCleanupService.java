package stackpot.stackpot.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import stackpot.stackpot.repository.TodoRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TodoCleanupService {

    private final TodoRepository todoRepository;

    // 매일 오전 3시 (03:00:00)에 실행
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional  // 트랜잭션 추가
    public void deleteOldTodos() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        int deletedCount = todoRepository.deleteByCreatedAtBefore(yesterday);
        System.out.println("[" + LocalDateTime.now() + "] " + deletedCount + "개의 오래된 TODO 항목이 삭제되었습니다.");
    }
}
