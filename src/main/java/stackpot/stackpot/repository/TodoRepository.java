package stackpot.stackpot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stackpot.stackpot.domain.mapping.UserTodo;

import java.time.LocalDateTime;

public interface TodoRepository extends JpaRepository<UserTodo, Long> {
    // 하루 전 createdAt 기준으로 데이터 삭제
    int deleteByCreatedAtBefore(LocalDateTime date);
}
