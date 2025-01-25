package stackpot.stackpot.repository.PotRepository;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.domain.mapping.PotApplication;
import stackpot.stackpot.domain.mapping.UserTodo;

import java.util.List;
import java.util.Optional;

public interface PotRepository extends JpaRepository<Pot, Long> {
    Page<Pot> findByRecruitmentDetails_RecruitmentRole(Role role, Pageable pageable);
    Optional<Pot> findPotWithRecruitmentDetailsByPotId(Long potId);
    List<Pot> findByPotApplication_User_Id(Long userId);
    List<Pot> findByUserId(Long userId);
    Page<Pot> findAll(Pageable pageable);
}
