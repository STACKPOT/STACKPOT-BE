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
import stackpot.stackpot.task.service.TaskQueryService;
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
    private final TaskQueryService taskQueryService;

    @Override
    public Badge getBadge(Long badgeId) {
        return badgeRepository.findBadgeByBadgeId(badgeId)
                .orElseThrow(() -> new PotHandler(BADGE_NOT_FOUND));
    }

    @Transactional
    @Override
    public void assignBadgeToTopMembers(Long potId) {
        // case [2] 총 팀 멤버가 2명 이하 -> 배지 부여 X (아무 동작 안 함)
        long memberCount = potMemberRepository.countByPot_PotId(potId);
        if (memberCount <= 2) return;

        // 완료한 '서로 다른 사용자 수' 집계
        long completedUserCount =
                userTodoRepository.countDistinctUserIdsByPotAndStatus(potId, TodoStatus.COMPLETED);

        // case [3] 팀 멤버 2명 이상 && 완료 사용자 수 < 2 -> 에러
        if (completedUserCount < 2) {
            throw new PotHandler(ErrorStatus.BADGE_INSUFFICIENT_TODO_COUNTS);
        }

        // case [1] 팀 멤버 2명 이상 && 완료 사용자 수 >= 2 -> 정상 (상위 2명 배지 부여)
        List<Long> topUserIds = userTodoRepository.findTop2UserIds(potId, TodoStatus.COMPLETED);
        if (topUserIds.size() < 2) {
            throw new PotHandler(ErrorStatus.BADGE_INSUFFICIENT_TOP_MEMBERS);
        }

        Badge badge = getBadge(1L);
        for (Long userId : topUserIds) {
            PotMember pm = potMemberRepository.findByPot_PotIdAndUser_Id(potId, userId)
                    .orElseThrow(() -> new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND));

            potMemberBadgeRepository.save(
                    PotMemberBadge.builder()
                            .badge(badge)
                            .potMember(pm)
                            .build()
            );
        }
    }

    @Transactional
    @Override
    public void assignTaskBadgeToTopMembers(Long potId) {
        List<Long> potMemberIds = potMemberRepository.selectPotMemberIdsByPotId(potId);
        if (potMemberIds.isEmpty()) {
            throw new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND);
        }

        List<PotMember> top2PotMembers = taskQueryService.getTop2TaskCountByPotMemberId(potMemberIds);
        if (top2PotMembers.size() < 2) {
            throw new PotHandler(ErrorStatus.BADGE_INSUFFICIENT_TOP_MEMBERS);
        }

        Badge badge = getBadge(2L);
        List<PotMemberBadge> newBadges = top2PotMembers.stream()
                .filter(pm -> !potMemberBadgeRepository.existsByBadgeAndPotMember(pm.getPotMemberId(),badge.getBadgeId()))
                .map(pm -> PotMemberBadge.builder().badge(badge).potMember(pm).build())
                .collect(Collectors.toList());
        if (!newBadges.isEmpty()) {
            potMemberBadgeRepository.saveAll(newBadges);
        }
    }
}

