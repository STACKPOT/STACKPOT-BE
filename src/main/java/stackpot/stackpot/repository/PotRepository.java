package stackpot.stackpot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stackpot.stackpot.domain.Pot;

import java.util.List;

public interface PotRepository extends JpaRepository<Pot, Long> {
    @Query("SELECT p FROM Pot p JOIN p.recruitmentDetails r WHERE r.recruitmentRole = :role")
    List<Pot> findByRecruitmentRole(@Param("role") String role);
}
