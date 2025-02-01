package stackpot.stackpot.service;

import stackpot.stackpot.domain.Badge;

public interface BadgeService {
    Badge getBadgeById(Long badgeId);
    Badge getDefaultBadge();
}

