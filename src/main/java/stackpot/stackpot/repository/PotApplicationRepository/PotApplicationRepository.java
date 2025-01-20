package stackpot.stackpot.repository.PotApplicationRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import stackpot.stackpot.domain.mapping.PotApplication;

import java.util.List;

public interface PotApplicationRepository extends JpaRepository<PotApplication, Long> {
    List<PotApplication> findByPot_PotId(Long potId);
}