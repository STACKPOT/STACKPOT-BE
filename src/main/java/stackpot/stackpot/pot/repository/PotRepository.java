package stackpot.stackpot.pot.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.user.entity.enums.Role;

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

    Page<Pot> findByUserIdAndPotStatus(Long userId, String potStatus, Pageable pageable);

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

    @Modifying
    @Query("DELETE FROM Pot f WHERE f.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    boolean existsByUserId(Long userId);

    // 지원자 수 기준으로 모든 Pot 정렬
    @Query("SELECT p FROM Pot p LEFT JOIN PotApplication pa ON p = pa.pot " +
            "GROUP BY p " +
            "ORDER BY COUNT(pa.applicationId) DESC, p.createdAt DESC")
    Page<Pot> findAllOrderByApplicantsCountDesc(Pageable pageable);

    /// 특정 Role을 기준으로 지원자 수 많은 순 정렬
    @Query("SELECT p FROM Pot p " +
            "LEFT JOIN PotRecruitmentDetails prd ON p = prd.pot " +
            "LEFT JOIN PotApplication pa ON p = pa.pot " +
            "WHERE prd.recruitmentRole = :recruitmentRole " +
            "GROUP BY p " +
            "ORDER BY COUNT(pa.applicationId) DESC, p.createdAt DESC")
    Page<Pot> findByRecruitmentRoleOrderByApplicantsCountDesc(@Param("recruitmentRole") Role recruitmentRole, Pageable pageable);

    List<Pot> findByPotMembers_UserIdOrderByCreatedAtDesc(Long userId);
}
