package stackpot.stackpot.pot.converter;

import stackpot.stackpot.common.util.DateFormatter;
import stackpot.stackpot.common.util.DdayCounter;
import stackpot.stackpot.common.util.RoleNameMapper;
import stackpot.stackpot.pot.dto.*;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.entity.PotRecruitmentDetails;
import stackpot.stackpot.pot.entity.enums.PotModeOfOperation;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.entity.enums.Role;

import java.util.List;

import org.springframework.stereotype.Component;


import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.time.format.DateTimeFormatter;

@Component
public class PotConverter{

    public Pot toEntity(PotRequestDto requestDto, User user) {
        return Pot.builder()
                .potName(requestDto.getPotName())
                .potStartDate(requestDto.getPotStartDate())
                .potDuration(requestDto.getPotDuration())
                .potLan(requestDto.getPotLan())
                .potContent(requestDto.getPotContent())
                .potModeOfOperation(PotModeOfOperation.valueOf(requestDto.getPotModeOfOperation()))
                .potSummary(requestDto.getPotSummary())
                .recruitmentDeadline(requestDto.getRecruitmentDeadline())
                .user(user)
                .build();
    }

    public PotResponseDto toDto(Pot entity, List<PotRecruitmentDetails> recruitmentDetails) {
        return PotResponseDto.builder()
                .potId(entity.getPotId())
                .potName(entity.getPotName())
                .potStartDate(DateFormatter.dotFormatter(entity.getPotStartDate()))
                .potEndDate(DateFormatter.dotFormatter(entity.getPotEndDate()))
                .potDuration(entity.getPotDuration())
                .potLan(entity.getPotLan())
                .potContent(entity.getPotContent())
                .potStatus(entity.getPotStatus())
                .potModeOfOperation(entity.getPotModeOfOperation().name())
                .potSummary(entity.getPotSummary())
                .recruitmentDeadline(entity.getRecruitmentDeadline())
                .recruitmentDetails(recruitmentDetails.stream().map(r ->
                        PotRecruitmentResponseDto.builder()
                                .recruitmentRole(r.getRecruitmentRole().name())
                                .recruitmentCount(r.getRecruitmentCount())
                                .build()
                ).collect(Collectors.toList()))
                .build();
    }

    public PotPreviewResponseDto toPrviewDto(User user, Pot pot, List<String> recruitmentRoles) {
        String dDay = DdayCounter.dDayCount(pot.getRecruitmentDeadline());

        List<String> koreanRoles = recruitmentRoles.stream()
                .map(RoleNameMapper::getKoreanRoleName)
                .collect(Collectors.toList());

        return PotPreviewResponseDto.builder()
                .userId(user.getId())
                .userRole(String.valueOf(user.getRole()))
                .userNickname(user.getNickname() + RoleNameMapper.mapRoleName(user.getRole().name()))
                .potId(pot.getPotId())
                .potName(pot.getPotName())
                .potContent(pot.getPotContent())
                .recruitmentRoles(koreanRoles)
                .dDay(dDay)
                .build();
    }

    public CompletedPotResponseDto toCompletedPotResponseDto(Pot pot, String formattedMembers, Role userPotRole) {
        Map<String, Integer> roleCountMap = pot.getPotMembers().stream()
                .collect(Collectors.groupingBy(
                        member -> member.getRoleName().name(),
                        Collectors.reducing(0, e -> 1, Integer::sum)
                ));

        return CompletedPotResponseDto.builder()
                .potId(pot.getPotId())
                .potName(pot.getPotName())
                .potStartDate(DateFormatter.dotFormatter(pot.getPotStartDate()))
                .potEndDate(DateFormatter.dotFormatter(pot.getPotEndDate()))
                .potLan(pot.getPotLan())
                .members(formattedMembers)
                .userPotRole(RoleNameMapper.getKoreanRoleName(userPotRole.name()))
                .memberCounts(roleCountMap)
                .build();
    }

    public PotSearchResponseDto toSearchDto(Pot pot) {
        String roleName = (pot.getUser() != null && pot.getUser().getRole() != null)
                ? pot.getUser().getRole().name()
                : "멤버";

        String nicknameWithRole = (pot.getUser() != null && pot.getUser().getNickname() != null)
                ? pot.getUser().getNickname() + " " + RoleNameMapper.mapRoleName(roleName)
                : "Unknown 멤버";

        return PotSearchResponseDto.builder()
                .potId(pot.getPotId())
                .potName(pot.getPotName())
                .potContent(pot.getPotContent())
                .creatorNickname(nicknameWithRole)
                .creatorRole(roleName)
                .recruitmentPart(
                        pot.getRecruitmentDetails() != null
                                ? pot.getRecruitmentDetails().stream()
                                .filter(rd -> rd.getRecruitmentRole() != null)
                                .map(rd -> rd.getRecruitmentRole().name())
                                .collect(Collectors.joining(", "))
                                : "없음"
                )
                .recruitmentDeadline(pot.getRecruitmentDeadline())
                .build();
    }

}
