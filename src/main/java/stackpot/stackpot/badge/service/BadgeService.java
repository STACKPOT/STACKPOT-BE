package stackpot.stackpot.badge.service;

import stackpot.stackpot.badge.entity.Badge;

public interface BadgeService {
    Badge getDefaultBadge();
    void assignBadgeToTopMembers(Long potId);
}

