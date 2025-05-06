package stackpot.stackpot.badge.service;

import stackpot.stackpot.badge.entity.Badge;

public interface BadgeService {
    Badge getBadgeById(Long badgeId);
    Badge getDefaultBadge();
    void assignBadgeToTopMembers(Long potId);
}

