package stackpot.stackpot.repository;

import org.aspectj.apache.bcel.generic.LOOKUPSWITCH;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.domain.mapping.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
}
