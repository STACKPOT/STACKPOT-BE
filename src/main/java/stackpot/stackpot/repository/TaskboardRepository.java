package stackpot.stackpot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.domain.Taskboard;
@Repository
public interface TaskboardRepository extends JpaRepository<Taskboard, Long> {
}
