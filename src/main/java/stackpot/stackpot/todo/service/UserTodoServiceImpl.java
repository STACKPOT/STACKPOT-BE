package stackpot.stackpot.todo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stackpot.stackpot.badge.service.BadgeService;
import stackpot.stackpot.todo.converter.UserTodoConverter;
import stackpot.stackpot.badge.entity.Badge;
import stackpot.stackpot.pot.entity.mapping.PotMember;
import stackpot.stackpot.badge.entity.mapping.PotMemberBadge;
import stackpot.stackpot.badge.repository.PotMemberBadgeRepository;
import stackpot.stackpot.todo.repository.UserTodoRepository;
import stackpot.stackpot.pot.repository.PotMemberRepository;
import stackpot.stackpot.todo.dto.UserTodoTopMemberDto;

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
    private final BadgeService badgeService;
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
                .map(user -> potMemberRepository.findByPot_PotIdAndUser_Id(potId, user.getUserId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        Badge badge = badgeService.getDefaultBadge(); // 변경: 기본 뱃지(1번 뱃지) 가져오기

        for (PotMember potMember : topPotMembers) {
            PotMemberBadge potMemberBadge = PotMemberBadge.builder()
                    .badge(badge)
                    .potMember(potMember)
                    .build();
            potMemberBadgeRepository.save(potMemberBadge);
        }

    }
}

