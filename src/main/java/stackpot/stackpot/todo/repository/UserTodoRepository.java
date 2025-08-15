package stackpot.stackpot.todo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.todo.entity.enums.TodoStatus;
import stackpot.stackpot.todo.entity.mapping.UserTodo;

import java.util.List;
import org.springframework.data.domain.Pageable;


@Repository
public interface UserTodoRepository extends JpaRepository<UserTodo, Long> {

    @Modifying
    @Query("DELETE FROM UserTodo f WHERE f.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM UserTodo f WHERE f.pot.potId = :potId")
    void deleteByPotId(@Param("potId") Long potId);

    long countByPot_PotIdAndStatus(Long potPotId, TodoStatus status);

    // 완료한 서로 다른 사용자 수 (distinct)
    @Query("""
        SELECT COUNT(DISTINCT ut.user.id)
        FROM UserTodo ut
        WHERE ut.pot.potId = :potId
          AND ut.status = :status
    """)
    long countDistinctUserIdsByPotAndStatus(@Param("potId") Long potId,
                                            @Param("status") TodoStatus status);

    // 완료 개수 기준 상위 사용자 조회 (페이징 적용)
    @Query("""
        SELECT ut.user.id
        FROM UserTodo ut
        WHERE ut.pot.potId = :potId
          AND ut.status = :status
        GROUP BY ut.user.id
        ORDER BY COUNT(ut.todoId) DESC
    """)
    Page<Long> findTopUserIdsByPotAndStatus(@Param("potId") Long potId,
                                            @Param("status") TodoStatus status,
                                            Pageable pageable);

    // 상위 2명 편의 메서드
    default List<Long> findTop2UserIds(Long potId, TodoStatus status) {
        return findTopUserIdsByPotAndStatus(potId, status, PageRequest.of(0, 2)).getContent();
    }


}
