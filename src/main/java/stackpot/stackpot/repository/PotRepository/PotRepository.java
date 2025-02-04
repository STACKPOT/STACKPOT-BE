package stackpot.stackpot.repository.PotRepository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.enums.Role;

import java.util.List;
import java.util.Optional;

public interface PotRepository extends JpaRepository<Pot, Long> {
    Page<Pot> findByRecruitmentDetails_RecruitmentRole(Role recruitmentRole, Pageable pageable);

    Optional<Pot> findPotWithRecruitmentDetailsByPotId(Long potId);

    List<Pot> findByPotApplication_User_Id(Long userId);

    List<Pot> findByUserId(Long userId);

    Page<Pot> findAll(Pageable pageable);

    List<Pot> findByPotMembers_UserIdAndPotStatusOrderByCreatedAtDesc(Long userId, String status);

    List<Pot> findByUserIdAndPotStatus(Long userId, String status);

    @Query("SELECT p FROM Pot p " +
            "WHERE LOWER(p.potName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.potContent) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY p.createdAt DESC")
    Page<Pot> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);


    @Query("SELECT p FROM Pot p WHERE p.potStatus = 'COMPLETED' " +
            "AND (p.user.id = :userId OR p.potId IN " +
            "(SELECT pm.pot.potId FROM PotMember pm WHERE pm.user.id = :userId)) " +
            "AND (:cursor IS NULL OR p.potId < :cursor) " +
            "ORDER BY p.potId DESC")
    List<Pot> findCompletedPotsByCursor(@Param("userId") Long userId, @Param("cursor") Long cursor);

    @Query("SELECT p FROM Pot p WHERE p.potStatus = 'COMPLETED' AND EXISTS (" +
            "SELECT pm FROM PotMember pm WHERE pm.pot = p AND pm.user.id = :userId)")
    List<Pot> findCompletedPotsByUserId(@Param("userId") Long userId);

    List<Pot> findByPotMembers_User_Id(Long potMembersUserId);

    @Query("SELECT p FROM Pot p WHERE p.user.id = :userId AND p.potStatus = 'COMPLETED' AND (:cursor IS NULL OR p.potId < :cursor) ORDER BY p.potId DESC")
    List<Pot> findCompletedPotsCreatedByUser(@Param("userId") Long userId, @Param("cursor") Long cursor);
}
