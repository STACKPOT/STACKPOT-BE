package stackpot.stackpot.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.user.entity.TempUser;

import java.time.LocalDateTime;

@Repository
public interface TempUserRepository extends JpaRepository<TempUser,Long> {
    int deleteByCreatedAtBefore(LocalDateTime time);
}
