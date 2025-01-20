package stackpot.stackpot.repository.PotRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.mapping.PotApplication;

import java.util.List;
import java.util.Optional;

public interface PotRepository extends JpaRepository<Pot, Long> {
    List<Pot> findByRecruitmentDetails_RecruitmentRole(String recruitmentRole);
    Optional<Pot> findPotWithRecruitmentDetailsByPotId(Long potId);
    List<Pot> findByPotApplication_User_Id(Long userId);

}
