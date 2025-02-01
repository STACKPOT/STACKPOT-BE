package stackpot.stackpot.repository.PotRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.mapping.UserTodo;

import java.util.List;
import java.util.Optional;

public interface MyPotRepository extends JpaRepository<UserTodo, Long> {
    List<UserTodo> findByPot_PotId(Long potId);
    List<UserTodo> findByPotAndUser(Pot pot, User user);
    Optional<UserTodo> findByTodoIdAndPot_PotId(Long todoId, Long potId);
    void deleteByPot_PotIdAndUser(Long potId, User user);
    List<UserTodo> findByPot_PotIdAndUser(Long potId, User user);
    List<UserTodo> findByUserAndPot(User user, Pot pot);

}
