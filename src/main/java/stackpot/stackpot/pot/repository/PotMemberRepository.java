package stackpot.stackpot.pot.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.pot.dto.UserMemberIdDto;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.entity.mapping.PotMember;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.entity.enums.Role;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
// 8. PotMemberRepository
public interface PotMemberRepository extends JpaRepository<PotMember, Long> {
    @Query("SELECT pm.user.id FROM PotMember pm WHERE pm.pot.potId = :potId")
    List<Long> findUserIdsByPotId(@Param("potId") Long potId);

    @Query("SELECT pm FROM PotMember pm WHERE pm.pot.potId = :potId")
    List<PotMember> findByPotId(@Param("potId") Long potId);

    @Query("SELECT pm FROM PotMember pm WHERE pm.user.id = :userId")
    List<PotMember> findByUserId(@Param("userId") Long userId);

    @Query("SELECT pm.roleName, COUNT(pm) FROM PotMember pm WHERE pm.pot.potId = :potId GROUP BY pm.roleName")
    List<Object[]> findRoleCountsByPotId(@Param("potId") Long potId);

    @Modifying
    @Query("DELETE FROM PotMember pm WHERE pm.pot.potId = :potId AND pm.user.id = :userId")
    void deleteByPotIdAndUserId(@Param("potId") Long potId, @Param("userId") Long userId);

    Optional<PotMember> findByPotAndUser(Pot pot, User user);

    boolean existsByPotAndUser(Pot pot, User user);

    // 특정 Pot에 속한 사용자(userId)의 역할(Role) 찾기
    @Query("SELECT pm.roleName FROM PotMember pm WHERE pm.pot.potId = :potId AND pm.user.id = :userId")
    Optional<Role> findRoleByUserId(@Param("potId") Long potId, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM PotMember f WHERE f.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM PotMember pm WHERE pm.pot.potId = :potId")
    void deleteByPotId(@Param("potId") Long potId);

    @Query("SELECT pm FROM PotMember pm WHERE pm.pot.potId = :potId AND pm.user.id = :userId")
    PotMember findByPotIdAndUserId(Long potId, Long userId);

    @Modifying
    @Query("UPDATE PotMember pm SET pm.potApplication = null WHERE pm.pot.potId = :potId")
    int clearApplicationReferences(@Param("potId") Long potId);

    Optional<PotMember> findByPot_PotIdAndUser_Email(Long potId, String email);

    Optional<PotMember> findByPotPotIdAndUser(Long potId, User user);

    @Query("select pm.potMemberId from PotMember pm where pm.user.id = :userId and pm.pot.potId = :potId")
    Optional<Long> selectByPotMemberIdByUserIdAndPotId(@Param("userId") Long userId, @Param("potId") Long potId);

    @Query("select pm.potMemberId from PotMember pm where pm.user.id in :userIds and pm.pot.potId = :potId")
    List<Long> selectByPotMemberIdsByUserIdsAndPotId(@Param("userIds") List<Long> userIds, @Param("potId") Long potId);

    @Query("select pm.user.id from PotMember pm where pm.pot.potId = :potId")
    List<Long> selectUserIdsAboutPotMembersByPotId(@Param("potId") Long potId);

    @Query("select new stackpot.stackpot.pot.dto.UserMemberIdDto(pm.potMemberId, pm.pot.potId) from PotMember pm where pm.user.id = :userId")
    List<UserMemberIdDto> selectPotMemberIdsByUserId(@Param("userId") Long userId);

    @Query("select pm from PotMember pm where pm.potMemberId in :potMemberIds")
    List<PotMember> selectPotMembersByPotMemberIds(@Param("potMemberIds") List<Long> potMemberIds);

    @Query("select pm.roleName from PotMember pm where pm.user.id = :userId and pm.pot.potId = :potId")
    Optional<Role> selectRoleByUserIdAndPotId(@Param("userId") Long userId, @Param("potId") Long potId);

    PotMember findByPot_PotIdAndOwnerTrue(Long potId);

    @Query("SELECT pm.potMemberId FROM PotMember pm WHERE pm.pot.potId = :potId")
    List<Long> selectPotMemberIdsByPotId(@Param("potId") Long potId);

    @Query("SELECT pm.pot.potId FROM PotMember pm WHERE pm.user.id = :userId AND pm.pot.potId IN :potIds")
    Set<Long> findPotIdsByUserIdAndPotIds(@Param("userId") Long userId, @Param("potIds") List<Long> potIds);


    @Modifying
    @Query("DELETE FROM PotMember pm WHERE pm.pot.potId IN :potIds AND pm.user.id = :userId")
    void deleteByUserIdAndPotIdIn(@Param("userId") Long userId, @Param("potIds") List<Long> potIds);

    long countByPot_PotId(Long potId);
    Optional<PotMember> findByPot_PotIdAndUser_Id(Long potId, Long userId);

    @Query("""
select pm.user.id as userId, pm.roleName as role
from PotMember pm
where pm.pot.potId = :potId
  and pm.user.id in :userIds
""")
    List<Object[]> findCreatorRolesByPotAndUserIds(@Param("potId") Long potId,
                                                   @Param("userIds") Collection<Long> userIds);

}
