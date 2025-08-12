package stackpot.stackpot.badge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.badge.entity.mapping.PotMemberBadge;

import java.util.List;

@Repository
public interface PotMemberBadgeRepository extends JpaRepository<PotMemberBadge, Long> {
    List<PotMemberBadge> findByPotMember_Pot_PotId(Long potId);
    List<PotMemberBadge> findByPotMember_Pot_PotIdAndPotMember_User_Id(Long potId, Long userId);

    @Modifying
    @Query("DELETE FROM PotMemberBadge b WHERE b.potMember.potMemberId IN :potMemberIds")
    void deleteByPotMemberIds(@Param("potMemberIds") List<Long> potMemberIds);

    @Query("SELECT CASE WHEN count(b) > 0 THEN true ELSE false END FROM PotMemberBadge b WHERE b.potMember.potMemberId = :potMemberId AND b.badge.badgeId = :badgeId")
    boolean existsByBadgeAndPotMember(@Param("potMemberId") Long potMemberId, @Param("badgeId") Long badgeId);
}

