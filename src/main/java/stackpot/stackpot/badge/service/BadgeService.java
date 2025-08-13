package stackpot.stackpot.badge.service;

import stackpot.stackpot.badge.entity.Badge;

public interface BadgeService {
    Badge getBadge(Long badgeId);
    void assignBadgeToTopMembers(Long potId);
    void assignTaskBadgeToTopMembers(Long potId);
}

