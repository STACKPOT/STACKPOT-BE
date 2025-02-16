package stackpot.stackpot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.Taskboard;

import java.util.List;

@Repository
public interface TaskboardRepository extends JpaRepository<Taskboard, Long> {
        List<Taskboard> findByPot(Pot pot);

        List<Taskboard> findByUserId(Long userId);

        Taskboard findByPotAndTaskboardId(Pot pot, Long taskboardId);

        @Modifying
        @Query("DELETE FROM Taskboard f WHERE f.user.id = :userId")
        void deleteByUserId(@Param("userId") Long userId);

}
