package stackpot.stackpot.todo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.todo.entity.enums.TodoStatus;
import stackpot.stackpot.todo.entity.mapping.UserTodo;

import java.util.List;

@Repository
public interface UserTodoRepository extends JpaRepository<UserTodo, Long> {

    @Query("SELECT u.id " +
            "FROM UserTodo t JOIN t.user u " +
            "WHERE t.pot.potId = :potId " +
            "GROUP BY u.id ORDER BY COUNT(t) DESC")
    List<Long> findTop2UserIds(@Param("potId") Long potId);

    @Modifying
    @Query("DELETE FROM UserTodo f WHERE f.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM UserTodo f WHERE f.pot.potId = :potId")
    void deleteByPotId(@Param("potId") Long potId);

    long countByPot_PotIdAndStatus(Long potPotId, TodoStatus status);

}
