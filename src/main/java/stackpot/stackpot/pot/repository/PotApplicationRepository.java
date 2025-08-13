package stackpot.stackpot.pot.repository;

import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import stackpot.stackpot.pot.entity.mapping.PotApplication;

import java.util.List;
import java.util.Optional;

public interface PotApplicationRepository extends JpaRepository<PotApplication, Long> {
    List<PotApplication> findByPot_PotId(Long potId);
    boolean existsByUserIdAndPot_PotId(Long userId, Long potId);
    // 특정 사용자의 특정 팟 지원 내역 조회
    Optional<PotApplication> findByUserIdAndPot_PotId(Long userId, Long potId);
    @Modifying
    @Query("DELETE FROM PotApplication f WHERE f.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM PotApplication f WHERE f.pot.potId = :potId")
    void deleteByPotId(@Param("potId") Long potId);

    @Modifying
    @Query("DELETE FROM PotApplication pa WHERE pa.pot.potId IN :potIds")
    @QueryHints({ @QueryHint(name = "javax.persistence.query.timeout", value = "5000") })
    void deleteByPotIds(@Param("potIds") List<Long> potIds);


}