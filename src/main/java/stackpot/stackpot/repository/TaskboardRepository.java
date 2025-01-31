package stackpot.stackpot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.Taskboard;

import java.util.List;

@Repository
public interface TaskboardRepository extends JpaRepository<Taskboard, Long> {
        List<Taskboard> findByPot(Pot pot);
}
