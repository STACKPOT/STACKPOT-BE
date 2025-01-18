package stackpot.stackpot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stackpot.stackpot.domain.Pot;

import java.util.List;
import java.util.Optional;

public interface PotRepository extends JpaRepository<Pot, Long> {
    @Query("SELECT p FROM Pot p JOIN p.recruitmentDetails r WHERE r.recruitmentRole = :role")
    List<Pot> findByRecruitmentRole(@Param("role") String role);

    @Query("SELECT p FROM Pot p LEFT JOIN FETCH p.recruitmentDetails r WHERE p.potId = :potId")
    Optional<Pot> findPotWithRecruitmentDetailsById(@Param("potId") Long potId);
}
