package stackpot.stackpot.pot.converter;

import stackpot.stackpot.badge.dto.BadgeDto;
import stackpot.stackpot.common.util.DateFormatter;
import stackpot.stackpot.common.util.DdayCounter;
import stackpot.stackpot.common.util.OperationModeMapper;
import stackpot.stackpot.common.util.RoleNameMapper;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.entity.PotRecruitmentDetails;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.pot.dto.CompletedPotDetailResponseDto;
import stackpot.stackpot.pot.dto.PotDetailResponseDto;

import org.springframework.stereotype.Component;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PotDetailConverter{
    public CompletedPotDetailResponseDto toCompletedPotDetailDto(String appealContent, String userPotRole, List<BadgeDto> myBadges) {
        return CompletedPotDetailResponseDto.builder()
                .appealContent(appealContent)
                .userPotRole(userPotRole)
                .myBadges(myBadges)
                .build();
    }

    public PotDetailResponseDto toPotDetailResponseDto(User user, Pot pot, String recruitmentDetails, Boolean isOwner, Boolean isApplied, Boolean isSaved) {
        String dDay = DdayCounter.dDayCount(pot.getRecruitmentDeadline());

        Map<String, Integer> recruitingMembers = pot.getRecruitmentDetails().stream()
                .collect(Collectors.toMap(
                        rd -> rd.getRecruitmentRole().name(),
                        PotRecruitmentDetails::getRecruitmentCount
                ));

        return PotDetailResponseDto.builder()
                .userId(user.getId())
                .userRole(user.getRole().name())
                .userNickname(user.getNickname() + RoleNameMapper.mapRoleName(user.getRole().name()))
                .isOwner(isOwner)
                .potId(pot.getPotId())
                .potName(pot.getPotName())
                .potStartDate(DateFormatter.dotFormatter(pot.getPotStartDate()))
                .potEndDate(DateFormatter.dotFormatter(pot.getPotEndDate()))
                .potDuration(pot.getPotDuration())
                .potLan(pot.getPotLan())
                .potStatus(pot.getPotStatus())
                .applied(isApplied)
                .potModeOfOperation(OperationModeMapper.getKoreanMode(pot.getPotModeOfOperation().name()))
                .potContent(pot.getPotContent())
                .potSummary(pot.getPotSummary())
                .dDay(dDay)
                .isSaved(isSaved)
                .recruitmentDeadline(DateFormatter.dotFormatter(pot.getRecruitmentDeadline()))
                .recruitmentDetails(recruitmentDetails)
                .recruitingMembers(recruitingMembers)
                .build();
    }

}

