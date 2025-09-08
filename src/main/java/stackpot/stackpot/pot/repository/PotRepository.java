package stackpot.stackpot.pot.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.user.entity.enums.Role;

import java.util.List;
import java.util.Optional;

public interface PotRepository extends JpaRepository<Pot, Long> {
    Optional<Pot> findPotWithRecruitmentDetailsByPotId(Long potId);

    List<Pot> findByPotApplication_User_Id(Long userId);

    Page<Pot> findAll(Pageable pageable);

    List<Pot> findByPotMembers_UserIdAndPotStatusOrderByCreatedAtDesc(Long userId, String status);

    List<Pot> findByUserIdAndPotStatus(Long userId, String status);

    @Query("SELECT p FROM Pot p WHERE p.user.id = :userId AND p.potStatus = :potStatus ORDER BY p.createdAt DESC")
    Page<Pot> findByUserIdAndPotStatusOrderByCreatedAtDesc(@Param("userId") Long userId,
                                                           @Param("potStatus") String potStatus,
                                                           Pageable pageable);

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

    @Query("SELECT p FROM Pot p WHERE p.user.id = :userId AND p.potStatus = 'COMPLETED' AND (:cursor IS NULL OR p.potId < :cursor) ORDER BY p.potId DESC")
    List<Pot> findCompletedPotsCreatedByUser(@Param("userId") Long userId, @Param("cursor") Long cursor);

    boolean existsByUserId(Long userId);

    // 지원자 수 기준으로 모든 Pot 정렬
    @Query("SELECT p FROM Pot p LEFT JOIN PotApplication pa ON p = pa.pot " +
            "WHERE p.potStatus = :potStatus " +
            "GROUP BY p " +
            "ORDER BY p.createdAt DESC, COUNT(pa.applicationId) DESC ")
    Page<Pot> findAllOrderByApplicantsCountDesc(@Param("potStatus") String potStatus, Pageable pageable);

    /// 여러 Role을 기준으로 지원자 수 많은 순 정렬
    @Query("SELECT p FROM Pot p " +
            "LEFT JOIN PotRecruitmentDetails prd ON p = prd.pot " +
            "LEFT JOIN PotApplication pa ON p = pa.pot " +
            "WHERE prd.recruitmentRole IN :roles AND p.potStatus = :potStatus " +
            "GROUP BY p " +
            "ORDER BY p.createdAt DESC, COUNT(pa.applicationId) DESC ")
    Page<Pot> findByRecruitmentRolesInOrderByApplicantsCountDesc(@Param("roles") List<Role> roles, @Param("potStatus") String potStatus, Pageable pageable);

    List<Pot> findByPotMembers_UserIdOrderByCreatedAtDesc(Long userId);

    @Query("select p.potId from Pot p where p.user.id = :userId and p.potStatus = :status")
    List<Long> findIdsByUserIdAndStatus(@Param("userId") Long userId,
                                        @Param("status") String status);

    @Query("select p.potId from Pot p where p.user.id = :userId and p.potStatus not in :statuses")
    List<Long> findIdsByUserIdAndStatusNotIn(@Param("userId") Long userId,
                                             @Param("statuses") List<String> statuses);

    @Modifying @Transactional
    @Query("""
      delete from Pot p
      where p.user.id = :userId
        and p.potId in :potIds
        and p.potStatus = 'RECRUITING'
    """)
    void deleteByUserIdAndPotIds(@Param("userId") Long userId, @Param("potIds") List<Long> potIds);
}
