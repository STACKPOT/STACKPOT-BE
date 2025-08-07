package stackpot.stackpot.pot.converter;

import stackpot.stackpot.badge.dto.BadgeDto;
import stackpot.stackpot.badge.dto.CompletedPotBadgeResponseDto;
import stackpot.stackpot.common.util.DateFormatter;
import stackpot.stackpot.common.util.DdayCounter;
import stackpot.stackpot.common.util.RoleNameMapper;
import stackpot.stackpot.pot.dto.AppliedPotResponseDto;
import stackpot.stackpot.pot.dto.OngoingPotResponseDto;
import stackpot.stackpot.pot.dto.RecruitingPotResponseDto;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.entity.PotRecruitmentDetails;
import stackpot.stackpot.user.entity.enums.Role;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class MyPotConverter{

    public AppliedPotResponseDto convertToAppliedPotResponseDto(Pot pot, List<String> recruitmentRoles) {
        String dDay = DdayCounter.dDayCount(pot.getPotRecruitmentDeadline());

        Map<String, Integer> RecruitmentRoleCountMap = pot.getRecruitmentDetails().stream()
                .collect(Collectors.groupingBy(
                        member -> member.getRecruitmentRole().name(),
                        Collectors.reducing(0, e -> 1, Integer::sum)
                ));

        List<String> koreanRoles = recruitmentRoles.stream()
                .map(RoleNameMapper::getKoreanRoleName)
                .collect(Collectors.toList());

        return AppliedPotResponseDto.builder()
                .potId(pot.getPotId())
                .potName(pot.getPotName())
                .potStartDate(pot.getPotStartDate())
                .potEndDate(pot.getPotEndDate())
                .potRecruitmentDeadline(pot.getPotRecruitmentDeadline())
                .potStatus(pot.getPotStatus())
                .potModeOfOperation(String.valueOf(pot.getPotModeOfOperation()))
                .potContent(pot.getPotContent())
                .dDay(dDay)
                .recruitmentRoles(koreanRoles)
                .members(RecruitmentRoleCountMap)
                .build();
    }

    public OngoingPotResponseDto convertToOngoingPotResponseDto(Pot pot, Long userId) {
        String dDay = DdayCounter.dDayCount(pot.getPotRecruitmentDeadline());
        Map<String, Integer> memberRoleCountMap = pot.getPotMembers().stream()
                .collect(Collectors.groupingBy(
                        member -> member.getRoleName().name(),
                        Collectors.reducing(0, e -> 1, Integer::sum)
                ));


        return OngoingPotResponseDto.builder()
                .potId(pot.getPotId())
                .potName(pot.getPotName())
                .potStartDate(pot.getPotStartDate())
                .potEndDate(pot.getPotEndDate())
                .potRecruitmentDeadline(pot.getPotRecruitmentDeadline())
                .potStatus(pot.getPotStatus())
                .potModeOfOperation(String.valueOf(pot.getPotModeOfOperation()))
                .potContent(pot.getPotContent())
                .dDay(dDay)
                .isOwner(isOwnerCheck(userId, pot))
                .members(memberRoleCountMap)
                .isMember(null)
                .build();
    }

    public OngoingPotResponseDto convertToMyPagePotResponseDto(Pot pot, Long userId, Boolean isMember) {
        String dDay = DdayCounter.dDayCount(pot.getPotRecruitmentDeadline());
        Map<String, Integer> memberRoleCountMap = pot.getPotMembers().stream()
                .collect(Collectors.groupingBy(
                        member -> member.getRoleName().name(),
                        Collectors.reducing(0, e -> 1, Integer::sum)
                ));


        return OngoingPotResponseDto.builder()
                .potId(pot.getPotId())
                .potName(pot.getPotName())
                .potStartDate(pot.getPotStartDate())
                .potEndDate(pot.getPotEndDate())
                .potRecruitmentDeadline(pot.getPotRecruitmentDeadline())
                .potStatus(pot.getPotStatus())
                .potModeOfOperation(String.valueOf(pot.getPotModeOfOperation()))
                .potContent(pot.getPotContent())
                .dDay(dDay)
                .isOwner(isOwnerCheck(userId, pot))
                .members(memberRoleCountMap)
                .isMember(isMember)
                .build();
    }

    private Boolean isOwnerCheck(Long userId, Pot pot) {
        return userId.equals(pot.getUser().getId());
    }

    public CompletedPotBadgeResponseDto toCompletedPotBadgeResponseDto(Pot pot, String formattedMembers, Role userPotRole, List<BadgeDto> myBadges) {
        Map<String, Integer> roleCountMap = pot.getPotMembers().stream()
                .collect(Collectors.groupingBy(
                        member -> member.getRoleName().name(),
                        Collectors.reducing(0, e -> 1, Integer::sum)
                ));

        return CompletedPotBadgeResponseDto.builder()
                .potId(pot.getPotId())
                .potName(pot.getPotName())
                .potStartDate(pot.getPotStartDate())
                .potEndDate(pot.getPotEndDate())
                .potRecruitmentDeadline(pot.getPotRecruitmentDeadline())
                .potLan(pot.getPotLan())
                .members(formattedMembers)
                .userPotRole(RoleNameMapper.getKoreanRoleName(userPotRole.name()))
                .myBadges(myBadges)
                .memberCounts(roleCountMap)
                .build();
    }

    public RecruitingPotResponseDto convertToRecruitingPotResponseDto(Pot pot, Long userId) {
        Map<String, Integer> recruitmentCountMap = pot.getRecruitmentDetails().stream()
                .collect(Collectors.toMap(
                        detail -> detail.getRecruitmentRole().name(),
                        PotRecruitmentDetails::getRecruitmentCount,
                        Integer::sum
                ));

        String dDay = DdayCounter.dDayCount(pot.getPotRecruitmentDeadline());

        return RecruitingPotResponseDto.builder()
                .potId(pot.getPotId())
                .potName(pot.getPotName())
                .members(recruitmentCountMap)
                .dDay(dDay)
                .build();
    }

}