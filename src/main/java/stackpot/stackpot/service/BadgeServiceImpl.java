package stackpot.stackpot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stackpot.stackpot.domain.Badge;
import stackpot.stackpot.repository.BadgeRepository.BadgeRepository;

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

