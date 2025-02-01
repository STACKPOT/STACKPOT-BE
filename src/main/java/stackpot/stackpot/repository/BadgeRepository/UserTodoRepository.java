package stackpot.stackpot.repository.BadgeRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.domain.mapping.UserTodo;

import java.util.List;

@Repository
public interface UserTodoRepository extends JpaRepository<UserTodo, Long> {

    @Query("SELECT ut.user.id, COUNT(ut) as todoCount " +
            "FROM UserTodo ut " +
            "WHERE ut.status = 'COMPLETED' AND ut.pot.potId = :potId " +
            "GROUP BY ut.user.id " +
            "ORDER BY todoCount DESC " +
            "LIMIT 2")
    List<Object[]> findTop2UsersWithMostTodos(@Param("potId") Long potId);
}
