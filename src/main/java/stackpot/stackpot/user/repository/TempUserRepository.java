package stackpot.stackpot.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.user.entity.TempUser;

@Repository
public interface TempUserRepository extends JpaRepository<TempUser,Long> {
}
