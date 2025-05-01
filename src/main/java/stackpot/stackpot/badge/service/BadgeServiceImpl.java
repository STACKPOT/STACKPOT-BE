package stackpot.stackpot.badge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stackpot.stackpot.badge.entity.Badge;
import stackpot.stackpot.badge.repository.BadgeRepository;

@Service
@RequiredArgsConstructor
public class BadgeServiceImpl implements BadgeService {

    private final BadgeRepository badgeRepository;
    private static final Long DEFAULT_BADGE_ID = 1L;

    @Override
    public Badge getBadgeById(Long badgeId) {
        return badgeRepository.findById(badgeId)
                .orElseThrow(() -> new IllegalArgumentException("Badge not found"));
    }

    @Override
    public Badge getDefaultBadge() {
        return getBadgeById(DEFAULT_BADGE_ID);
    }
}

