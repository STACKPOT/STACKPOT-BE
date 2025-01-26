package stackpot.stackpot.converter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.PotRecruitmentDetails;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.PotModeOfOperation;
import stackpot.stackpot.web.dto.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.time.format.DateTimeFormatter;

@Component
public class PotConverterImpl implements PotConverter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    @Override
    public Pot toEntity(PotRequestDto requestDto, User user) {
        return Pot.builder()
                .potName(requestDto.getPotName())
                .potStartDate(requestDto.getPotStartDate())
                .potEndDate(requestDto.getPotEndDate())
                .potDuration(requestDto.getPotDuration())
                .potLan(requestDto.getPotLan())
                .potContent(requestDto.getPotContent())
                .potStatus(requestDto.getPotStatus())
                .potModeOfOperation(PotModeOfOperation.valueOf(requestDto.getPotModeOfOperation()))
                .potSummary(requestDto.getPotSummary())
                .recruitmentDeadline(requestDto.getRecruitmentDeadline())
                .user(user) // 사용자 설정
                .build();
    }

    @Override
    public PotResponseDto toDto(Pot entity, List<PotRecruitmentDetails> recruitmentDetails) {
        return PotResponseDto.builder()
                .potId(entity.getPotId())
                .potName(entity.getPotName())
                .potStartDate(formatDate(entity.getPotStartDate()))
                .potEndDate(formatDate(entity.getPotEndDate()))
                .potDuration(entity.getPotDuration())
                .potLan(entity.getPotLan())
                .potContent(entity.getPotContent())
                .potStatus(entity.getPotStatus())
                .potModeOfOperation(entity.getPotModeOfOperation().name())
                .potSummary(entity.getPotSummary())
                .recruitmentDeadline(entity.getRecruitmentDeadline())
                .recruitmentDetails(recruitmentDetails.stream().map(r -> PotRecruitmentResponseDto.builder()
                        .recruitmentId(r.getRecruitmentId())
                        .recruitmentRole(r.getRecruitmentRole().name())
                        .recruitmentCount(r.getRecruitmentCount())
                        .build()).collect(Collectors.toList()))
                .build();
    }

    private String formatDate(java.time.LocalDate date) {
        return (date != null) ? date.format(DATE_FORMATTER) : "N/A";
    }

    @Override

    public CompletedPotResponseDto toCompletedPotResponseDto(Pot pot, Map<String, Integer> roleCounts) {
        return CompletedPotResponseDto.builder()
                .potId(pot.getPotId())
                .potName(pot.getPotName())
                .potStartDate(pot.getPotStartDate())
                .potEndDate(pot.getPotEndDate())
                .potDuration(pot.getPotDuration())
                .potLan(pot.getPotLan())
                .potContent(pot.getPotContent())
                .potStatus(pot.getPotStatus())
                .potModeOfOperation(pot.getPotModeOfOperation())
                .potSummary(pot.getPotSummary())
                .recruitmentDeadline(pot.getRecruitmentDeadline())
                .recruitmentDetails(pot.getRecruitmentDetails().stream()
                        .map(rd -> RecruitmentDetailResponseDto.builder()
                                .recruitmentRole(String.valueOf(rd.getRecruitmentRole()))
                                .recruitmentCount(rd.getRecruitmentCount())
                                .build())
                        .collect(Collectors.toList()))
                .roleCounts(roleCounts)
                .build();
    }

    public PotSearchResponseDto toSearchDto(Pot pot) {
        // 역할 이름 매핑 (유효한 역할만 처리)
        String roleName = pot.getUser() != null && pot.getUser().getRole() != null
                ? pot.getUser().getRole().name()
                : "멤버";

        String nicknameWithRole = pot.getUser() != null && pot.getUser().getNickname() != null
                ? pot.getUser().getNickname() + " " + mapRoleName(roleName)
                : "Unknown 멤버";

        return PotSearchResponseDto.builder()
                .potId(pot.getPotId())
                .potName(pot.getPotName())
                .potContent(pot.getPotContent())
                .creatorNickname(nicknameWithRole)
                .creatorRole(
                        pot.getUser() != null && pot.getUser().getRole() != null
                                ? pot.getUser().getRole().name()
                                : "멤버" // 기본값 설정
                )
                .recruitmentPart(
                        pot.getRecruitmentDetails() != null
                                ? pot.getRecruitmentDetails().stream()
                                .filter(rd -> rd.getRecruitmentRole() != null)
                                .map(rd -> rd.getRecruitmentRole().name())
                                .collect(Collectors.joining(", "))
                                : "없음" // 기본값 설정
                )
                .recruitmentDeadline(pot.getRecruitmentDeadline())
                .build();
    }


    private String mapRoleName(String roleName) {
        return switch (roleName) {
            case "BACKEND" -> "양파";
            case "FRONTEND" -> "버섯";
            case "DESIGN" -> "브로콜리";
            case "PLANNING" -> "당근";
            default -> "멤버";
        };
    }
}