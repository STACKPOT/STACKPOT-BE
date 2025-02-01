package stackpot.stackpot.repository.PotRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.mapping.UserTodo;

import java.util.List;
import java.util.Optional;

public interface MyPotRepository extends JpaRepository<UserTodo, Long> {
    List<UserTodo> findByPot_PotId(Long potId);
    @Query("SELECT t FROM UserTodo t WHERE t.pot = :pot AND t.user IN :users")
    List<UserTodo> findByPotAndUsers(@Param("pot") Pot pot, @Param("users") List<User> users);
    Optional<UserTodo> findByTodoIdAndPot_PotId(Long todoId, Long potId);
    void deleteByPot_PotIdAndUser(Long potId, User user);
    List<UserTodo> findByPot_PotIdAndUser(Long potId, User user);
    List<UserTodo> findByUserAndPot(User user, Pot pot);

}
