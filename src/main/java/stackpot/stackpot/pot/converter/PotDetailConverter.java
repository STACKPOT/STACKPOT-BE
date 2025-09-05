package stackpot.stackpot.pot.converter;

import stackpot.stackpot.badge.dto.BadgeDto;
import stackpot.stackpot.common.util.DateFormatter;
import stackpot.stackpot.common.util.DdayCounter;
import stackpot.stackpot.common.util.OperationModeMapper;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.entity.PotRecruitmentDetails;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.pot.dto.AppealContentDto;
import stackpot.stackpot.pot.dto.PotDetailResponseDto;

import org.springframework.stereotype.Component;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PotDetailConverter{
    public AppealContentDto toCompletedPotDetailDto(String appealContent, String userPotRole, List<BadgeDto> myBadges) {
        return AppealContentDto.builder()
                .appealContent(appealContent)
                .userPotRole(userPotRole)
                .myBadges(myBadges)
                .build();
    }

    public PotDetailResponseDto toPotDetailResponseDto(User user, Pot pot, String recruitmentDetails, Boolean isOwner, Boolean isApplied, Boolean isSaved, Long commentCount, String creatorRoleName) {
        String dDay = DdayCounter.dDayCount(pot.getPotRecruitmentDeadline());

        Map<String, Integer> recruitingMembers = pot.getRecruitmentDetails().stream()
                .collect(Collectors.toMap(
                        rd -> rd.getRecruitmentRole().name(),
                        PotRecruitmentDetails::getRecruitmentCount
                ));

        return PotDetailResponseDto.builder()
                .userId(user.getId())
                .userRole(creatorRoleName)
                .userNickname(user.getNickname() + " 새싹")
                .isOwner(isOwner)
                .potId(pot.getPotId())
                .potName(pot.getPotName())
                .potStartDate(DateFormatter.formatToMonth(pot.getPotStartDate()))
                .potEndDate(DateFormatter.formatToMonth(pot.getPotEndDate()))
                .potRecruitmentDeadline(pot.getPotRecruitmentDeadline())
                .potLan(pot.getPotLan())
                .potStatus(pot.getPotStatus())
                .applied(isApplied)
                .potModeOfOperation(OperationModeMapper.getKoreanMode(pot.getPotModeOfOperation().name()))
                .potContent(pot.getPotContent())
                .potSummary(pot.getPotSummary())
                .dDay(dDay)
                .isSaved(isSaved)
                .potRecruitmentDeadline(pot.getPotRecruitmentDeadline())
                .recruitmentDetails(recruitmentDetails)
                .recruitingMembers(recruitingMembers)
                .commentCount(commentCount)
                .build();
    }

}

