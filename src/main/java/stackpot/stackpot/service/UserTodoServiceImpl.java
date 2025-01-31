package stackpot.stackpot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stackpot.stackpot.converter.UserTodoConverter;
import stackpot.stackpot.domain.Badge;
import stackpot.stackpot.domain.mapping.PotMember;
import stackpot.stackpot.domain.mapping.PotMemberBadge;
import stackpot.stackpot.repository.BadgeRepository.UserTodoRepository;
import stackpot.stackpot.repository.PotMemberRepository;
import stackpot.stackpot.web.dto.UserTodoTopMemberDto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserTodoServiceImpl implements UserTodoService {

    private final UserTodoRepository userTodoRepository;
    private final UserTodoConverter userTodoConverter;
    private final PotMemberRepository potMemberRepository;
    private final PotMemberBadgeRepository potMemberBadgeRepository;
    private final BadgeRepository badgeRepository;
    private static final Long DEFAULT_BADGE_ID = 1L;

    @Transactional
    @Override
    public void assignBadgeToTopMembers(Long potId) {
        List<Object[]> topUsers = userTodoRepository.findTop2UsersWithMostTodos(potId);

        if (topUsers.isEmpty()) {
            return;
        }

        List<UserTodoTopMemberDto> topMemberDtos = userTodoConverter.toTopMemberDto(topUsers);

        List<PotMember> topPotMembers = topMemberDtos.stream()
                .map(user -> potMemberRepository.findByPotIdAndUserId(potId, user.getUserId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        Badge badge = badgeRepository.findById(DEFAULT_BADGE_ID)
                .orElseThrow(() -> new IllegalArgumentException("Badge not found"));

        for (PotMember potMember : topPotMembers) {
            PotMemberBadge potMemberBadge = new PotMemberBadge();
            potMemberBadge.setPotMember(potMember);
            potMemberBadge.setBadge(badge);
            potMemberBadgeRepository.save(potMemberBadge);
        }
    }
}

