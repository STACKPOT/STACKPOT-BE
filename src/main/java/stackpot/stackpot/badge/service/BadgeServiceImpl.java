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
import stackpot.stackpot.todo.entity.enums.TodoStatus;
import stackpot.stackpot.todo.repository.UserTodoRepository;

import java.util.List;
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
        // 1. 완료된 Todo 개수가 0이면 예외
        long completedTodoCount = userTodoRepository.countByPot_PotIdAndStatus(potId, TodoStatus.COMPLETED);
        if (completedTodoCount == 0) {
            throw new PotHandler(ErrorStatus.BADGE_INSUFFICIENT_TODO_COUNTS);
        }

        // 2. Todo를 많이 완료한 상위 2명의 userId 조회
        List<Long> topUserIds = userTodoRepository.findTop2UserIds(potId, TodoStatus.COMPLETED);
        if (topUserIds.size() < 2) {
            throw new PotHandler(ErrorStatus.BADGE_INSUFFICIENT_TOP_MEMBERS);
        }

        // 3. PotMember 조회
        List<PotMember> topPotMembers = topUserIds.stream()
                .map(userId -> potMemberRepository.findByPot_PotIdAndUser_Id(potId, userId)
                        .orElseThrow(() -> new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND)))
                .collect(Collectors.toList());

        // 4. 기본 배지 부여
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

