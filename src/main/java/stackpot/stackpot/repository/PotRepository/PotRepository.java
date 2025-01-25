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
    Page<Pot> findByRecruitmentDetails_RecruitmentRole(Role recruitmentRole, Pageable pageable);
    Optional<Pot> findPotWithRecruitmentDetailsByPotId(Long potId);
    List<Pot> findByPotApplication_User_Id(Long userId);
    List<Pot> findByUserId(Long userId);
    Page<Pot> findAll(Pageable pageable);
    List<Pot> findByPotMembers_UserIdAndPotStatus(Long userId, String status);
    List<Pot> findByUserIdAndPotStatus(Long userId, String status);

    @Query("SELECT p FROM Pot p WHERE p.potStatus = 'COMPLETED' AND (p.user.id = :userId OR p.potId IN " +
            "(SELECT pm.pot.potId FROM PotMember pm WHERE pm.user.id = :userId))")
    List<Pot> findCompletedPotsByCreatorOrMember(@Param("userId") Long userId);
}
