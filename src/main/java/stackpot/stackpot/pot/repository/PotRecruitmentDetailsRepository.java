package stackpot.stackpot.pot.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stackpot.stackpot.pot.entity.PotRecruitmentDetails;

import java.util.List;

public interface PotRecruitmentDetailsRepository extends JpaRepository<PotRecruitmentDetails, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM PotRecruitmentDetails r WHERE r.pot.potId = :potId")
    void deleteByPot_PotId(@Param("potId") Long potId);


    @Modifying
    @Transactional
    @Query("DELETE FROM PotRecruitmentDetails prd WHERE prd.pot.potId IN :potIds")
    void deleteByPotIds(@Param("potIds") List<Long> potIds);
}