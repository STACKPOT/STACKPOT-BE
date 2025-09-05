package stackpot.stackpot.task.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.pot.entity.mapping.PotMember;
import stackpot.stackpot.task.entity.Taskboard;
import stackpot.stackpot.task.entity.mapping.Task;

import java.util.Collection;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByTaskboard(Taskboard taskboard);

    void deleteByTaskboard(Taskboard taskboard);

    @Modifying
    @Query("DELETE FROM Task t WHERE t.potMember.potMemberId IN :potMemberIds")
    void deleteByPotMemberIds(@Param("potMemberIds") List<Long> potMemberIds);

    @Modifying
    @Query("DELETE FROM Task t WHERE t.taskboard.taskboardId IN :taskboardIds")
    void deleteByTaskboardIds(@Param("taskboardIds") List<Long> taskboardIds);

    @Query("SELECT t.potMember " +
            "FROM Task t " +
            "WHERE t.potMember.potMemberId IN :potMemberIds " +
            "GROUP BY t.potMember.potMemberId " +
            "ORDER BY count(t) DESC")
    List<PotMember> getTop2TaskCountByPotMemberId(@Param("potMemberIds") List<Long> potMemberIds, Pageable pageable);

    List<Task> findByTaskboardIn(Collection<Taskboard> taskboards);



}
