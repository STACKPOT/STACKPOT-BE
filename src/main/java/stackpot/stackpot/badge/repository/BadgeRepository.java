package stackpot.stackpot.badge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.badge.entity.Badge;

import java.util.Optional;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
    Optional<Badge> findBadgeByBadgeId(Long badgeId);
}

