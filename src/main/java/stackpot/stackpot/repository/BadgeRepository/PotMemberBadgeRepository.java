package stackpot.stackpot.repository.BadgeRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.domain.mapping.PotMemberBadge;

@Repository
public interface PotMemberBadgeRepository extends JpaRepository<PotMemberBadge, Long> {
}

