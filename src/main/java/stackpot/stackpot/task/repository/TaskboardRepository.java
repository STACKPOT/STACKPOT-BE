package stackpot.stackpot.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.task.entity.Taskboard;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskboardRepository extends JpaRepository<Taskboard, Long> {
        List<Taskboard> findByPot(Pot pot);

        List<Taskboard> findByPotPotId(Long potId);

        List<Taskboard> findByUserId(Long userId);

        Optional<Taskboard> findByPotAndTaskboardId(Pot pot, Long taskboardId);

        @Modifying
        @Query("DELETE FROM Taskboard f WHERE f.user.id = :userId")
        void deleteByUserId(@Param("userId") Long userId);
        @Modifying
        @Query("DELETE FROM Taskboard f WHERE f.pot.potId = :potId")
        void deleteByPotId(@Param("potId") Long potId);

        @Query("SELECT t FROM Taskboard t WHERE t.pot.potId = :potId AND t.deadLine BETWEEN :startDate AND :endDate ORDER BY t.deadLine ASC")
        List<Taskboard> findByPotPIdAndDeadLineBetweenOrderByDeadLineAsc(
                @Param("potId") Long potId,
                @Param("startDate") LocalDate startDate,
                @Param("endDate") LocalDate endDate
        );

        @Query("SELECT t FROM Taskboard t WHERE t.pot.potId = :potId AND t.deadLine = :date")
        List<Taskboard> findByPotPotIdAndDeadLine(
                @Param("potId") Long potId,
                @Param("date") LocalDate date
        );


}
