package stackpot.stackpot.badge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.PotHandler;
import stackpot.stackpot.badge.converter.PotBadgeMemberConverter;
import stackpot.stackpot.badge.entity.Badge;
import stackpot.stackpot.badge.entity.mapping.PotMemberBadge;
import stackpot.stackpot.badge.repository.BadgeRepository;
import stackpot.stackpot.badge.repository.PotMemberBadgeRepository;
import stackpot.stackpot.pot.entity.mapping.PotMember;
import stackpot.stackpot.pot.repository.PotMemberRepository;
import stackpot.stackpot.todo.dto.UserTodoTopMemberDto;
import stackpot.stackpot.todo.entity.enums.TodoStatus;
import stackpot.stackpot.todo.repository.UserTodoRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static stackpot.stackpot.apiPayload.code.status.ErrorStatus.BADGE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class BadgeServiceImpl implements BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserTodoRepository userTodoRepository;
    private final PotMemberRepository potMemberRepository;
    private final PotMemberBadgeRepository potMemberBadgeRepository;
    private final PotBadgeMemberConverter potBadgeMemberConverter;

    private static final Long DEFAULT_BADGE_ID = 1L;

    @Override
    public Badge getDefaultBadge() {
        return badgeRepository.findBadgeByBadgeId(DEFAULT_BADGE_ID)
                .orElseThrow(() -> new PotHandler(BADGE_NOT_FOUND));
    }

    @Transactional
    @Override
    public void assignBadgeToTopMembers(Long potId) {
        long completedTodoCount = userTodoRepository.countByPot_PotIdAndStatus(potId, TodoStatus.COMPLETED);
        List<Object[]> topUsers = userTodoRepository.findTop2UsersWithMostTodos(potId);

        if (completedTodoCount == 0) throw new PotHandler(ErrorStatus.BADGE_INSUFFICIENT_TODO_COUNTS);
        if (topUsers.size() < 2) throw new PotHandler(ErrorStatus.BADGE_INSUFFICIENT_TOP_MEMBERS);

        List<UserTodoTopMemberDto> topMemberDtos = potBadgeMemberConverter.toTopMemberDto(topUsers);

        List<PotMember> topPotMembers = topMemberDtos.stream()
                .map(user -> potMemberRepository.findByPot_PotIdAndUser_Id(potId, user.getUserId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        Badge badge = getDefaultBadge();

        for (PotMember potMember : topPotMembers) {
            PotMemberBadge potMemberBadge = PotMemberBadge.builder()
                    .badge(badge)
                    .potMember(potMember)
                    .build();
            potMemberBadgeRepository.save(potMemberBadge);
        }
    }
}

