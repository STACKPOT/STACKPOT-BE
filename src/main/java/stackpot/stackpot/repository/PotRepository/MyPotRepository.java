package stackpot.stackpot.repository.PotRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.mapping.UserTodo;

import java.util.List;
import java.util.Optional;

public interface MyPotRepository extends JpaRepository<UserTodo, Long> {
}
