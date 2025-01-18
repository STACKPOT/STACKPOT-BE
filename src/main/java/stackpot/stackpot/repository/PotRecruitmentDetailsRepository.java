package stackpot.stackpot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stackpot.stackpot.domain.PotRecruitmentDetails;

public interface PotRecruitmentDetailsRepository extends JpaRepository<PotRecruitmentDetails, Long> {
}