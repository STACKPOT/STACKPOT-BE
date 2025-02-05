package stackpot.stackpot.repository.PotApplicationRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import stackpot.stackpot.domain.mapping.PotApplication;

import java.util.List;
import java.util.Optional;

public interface PotApplicationRepository extends JpaRepository<PotApplication, Long> {
    List<PotApplication> findByPot_PotId(Long potId);
    boolean existsByUserIdAndPot_PotId(Long userId, Long potId);
    // 특정 사용자의 특정 팟 지원 내역 조회
    Optional<PotApplication> findByUserIdAndPot_PotId(Long userId, Long potId);

}