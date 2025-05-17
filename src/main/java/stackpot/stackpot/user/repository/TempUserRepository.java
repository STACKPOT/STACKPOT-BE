package stackpot.stackpot.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stackpot.stackpot.user.entity.TempUser;

public interface TempUserRepository extends JpaRepository<TempUser,Long> {
}
