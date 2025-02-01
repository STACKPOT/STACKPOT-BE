package stackpot.stackpot.repository.BadgeRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.domain.mapping.PotMemberBadge;

import java.util.List;

@Repository
public interface PotMemberBadgeRepository extends JpaRepository<PotMemberBadge, Long> {
    List<PotMemberBadge> findByPotMember_Pot_PotId(Long potId);
    List<PotMemberBadge> findByPotMember_Pot_PotIdAndPotMember_User_Id(Long potId, Long userId);
}

