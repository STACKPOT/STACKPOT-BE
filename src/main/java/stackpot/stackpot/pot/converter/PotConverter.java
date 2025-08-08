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

import java.util.Arrays;
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
                .potEndDate(requestDto.getPotEndDate())
                .potRecruitmentDeadline(requestDto.getPotRecruitmentDeadline())
                .potLan(requestDto.getPotLan())
                .potContent(requestDto.getPotContent())
                .potModeOfOperation(PotModeOfOperation.valueOf(requestDto.getPotModeOfOperation()))
                .potSummary(requestDto.getPotSummary())
                .user(user)
                .build();
    }

    public PotResponseDto toDto(Pot entity, List<PotRecruitmentDetails> recruitmentDetails) {
        return PotResponseDto.builder()
                .potId(entity.getPotId())
                .potName(entity.getPotName())
                .potStartDate(entity.getPotStartDate())
                .potEndDate(entity.getPotEndDate())
                .potRecruitmentDeadline(entity.getPotRecruitmentDeadline())
                .potLan(entity.getPotLan())
                .potContent(entity.getPotContent())
                .potStatus(entity.getPotStatus())
                .potModeOfOperation(entity.getPotModeOfOperation().name())
                .potSummary(entity.getPotSummary())
                .recruitmentDetails(recruitmentDetails.stream().map(r ->
                        PotRecruitmentResponseDto.builder()
                                .recruitmentRole(r.getRecruitmentRole().name())
                                .recruitmentCount(r.getRecruitmentCount())
                                .build()
                ).collect(Collectors.toList()))
                .build();
    }

    public PotPreviewResponseDto toPrviewDto(User user, Pot pot, List<String> recruitmentRoles, boolean isSaved, int potSaveCount, boolean isMember) {
        String dDay = DdayCounter.dDayCount(pot.getPotRecruitmentDeadline());

        List<String> koreanRoles = recruitmentRoles.stream()
                .map(RoleNameMapper::getKoreanRoleName)
                .collect(Collectors.toList());

        return PotPreviewResponseDto.builder()
                .userId(user.getId())
                .userRole(user.getRole().name())
                .userNickname(user.getNickname() + " 새싹")
                .potId(pot.getPotId())
                .potName(pot.getPotName())
                .potContent(pot.getPotContent())
                .recruitmentRoles(koreanRoles)
                .dDay(dDay)
                .isSaved(isSaved)
                .potSaveCount(potSaveCount)
                .isMember(isMember)
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
                .potStartDate(pot.getPotStartDate())
                .potEndDate(pot.getPotEndDate())
                .potRecruitmentDeadline(pot.getPotRecruitmentDeadline())
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
                .recruitmentDeadline(pot.getPotRecruitmentDeadline())
                .build();
    }

    public PotSummaryDto toDto(Pot pot) {
        return PotSummaryDto.builder()
                .summary(pot.getPotSummary())
                .potLan(splitLanguages(pot.getPotLan()))
                .build();
    }

    private List<String> splitLanguages(String potLan) {
        if (potLan == null || potLan.isBlank()) return List.of();
        return Arrays.stream(potLan.split(","))
                .map(String::trim) // 공백 제거
                .filter(s -> !s.isEmpty()) // 빈 항목 제거
                .collect(Collectors.toList());
    }

}
