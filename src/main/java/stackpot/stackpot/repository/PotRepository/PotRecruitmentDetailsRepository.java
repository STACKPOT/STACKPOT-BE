package stackpot.stackpot.repository.PotRepository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stackpot.stackpot.domain.PotRecruitmentDetails;

public interface PotRecruitmentDetailsRepository extends JpaRepository<PotRecruitmentDetails, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM PotRecruitmentDetails r WHERE r.pot.potId = :potId")
    void deleteByPot_PotId(@Param("potId") Long potId);
}