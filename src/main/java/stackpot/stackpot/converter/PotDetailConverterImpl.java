package stackpot.stackpot.converter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.web.dto.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PotDetailConverterImpl implements PotDetailConverter {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy. MM. dd");

    @Override
    public CompletedPotDetailResponseDto toCompletedPotDetailDto(Pot pot, String userPotRole, String appealContent) {
        // 날짜 포맷 설정
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy. MM. dd");

        return CompletedPotDetailResponseDto.builder()
                .potId(pot.getPotId())
                .potName(pot.getPotName())
                .potStartDate(pot.getPotStartDate() != null ? pot.getPotStartDate().format(formatter) : null)
                .potEndDate(pot.getPotEndDate() != null ? pot.getPotEndDate().format(formatter) : null)
                .potContent(pot.getPotContent())
                .potStatus(pot.getPotStatus())
                .potSummary(pot.getPotSummary())
                .appealContent(appealContent)
                .userPotRole(userPotRole)
                .build();
    }

    @Override
    public PotDetailResponseDto toPotDetailResponseDto(User user, Pot pot, String recruitmentDetails, Boolean isOwner, Boolean isApplied){
        LocalDate today = LocalDate.now();
        LocalDate deadline = pot.getRecruitmentDeadline();

        long daysDiff = ChronoUnit.DAYS.between(today, deadline);

        String dDay;
        if (daysDiff == 0) {
            dDay = "D-Day";
        } else if (daysDiff > 0) {
            dDay = "D-" + daysDiff;
        } else {
            dDay = "D+" + Math.abs(daysDiff);
        }

        // recruitmentDetails를 Map<String, Integer> 형태로 변환
        Map<String, Integer> recruitingMembers = pot.getRecruitmentDetails().stream()
                .collect(Collectors.toMap(
                        recruitmentDetail -> getKoreanRoleName(recruitmentDetail.getRecruitmentRole().name()),
                        recruitmentDetail -> recruitmentDetail.getRecruitmentCount()
                ));

        return PotDetailResponseDto.builder()
                .userId(user.getId())
                .userRole(String.valueOf(user.getRole()))
                .userNickname(user.getNickname() + getVegetableNameByRole(user.getRole().name()))
                .isOwner(isOwner)
                .potId(pot.getPotId())
                .potName(pot.getPotName())
                .potStartDate(formatDate(pot.getPotStartDate()))
                .potDuration(pot.getPotDuration())
                .potLan(pot.getPotLan())
                .potStatus(pot.getPotStatus())
                .applied(isApplied)
                .potModeOfOperation(getKoreanModeOfOperation(String.valueOf(pot.getPotModeOfOperation())))
                .potContent(pot.getPotContent())
                .dDay(dDay)
                .recruitmentDeadline(formatDate(pot.getRecruitmentDeadline()))
                .recruitmentDetails(recruitmentDetails)
                .recruitingMembers(recruitingMembers)
                .build();
    }

    @Override
    public AppliedPotResponseDto toAppliedPotResponseDto(User user, Pot pot, String recruitmentDetails) {
        LocalDate today = LocalDate.now();
        LocalDate deadline = pot.getRecruitmentDeadline();

        long daysDiff = ChronoUnit.DAYS.between(today, deadline);

        String dDay;
        if (daysDiff == 0) {
            dDay = "D-Day";
        } else if (daysDiff > 0) {
            dDay = "D-" + daysDiff;
        } else {
            dDay = "D+" + Math.abs(daysDiff);
        }

        return AppliedPotResponseDto.builder()
                .userId(user.getId())
                .userRole(String.valueOf(user.getRole()))
                .userNickname(user.getNickname() + getVegetableNameByRole(user.getRole().name()))
                .potId(pot.getPotId())
                .potStatus(getPotStatus(pot.getPotStatus()))
                .potName(pot.getPotName())
                .potStartDate(formatDate(pot.getPotStartDate()))
                .potDuration(pot.getPotDuration())
                .potLan(pot.getPotLan())
                .potModeOfOperation(getKoreanModeOfOperation(String.valueOf(pot.getPotModeOfOperation())))
                .potContent(pot.getPotContent())
                .dDay(dDay)
                .recruitmentDetails(recruitmentDetails)
                .build();
    }

    private String getVegetableNameByRole(String role) {
        Map<String, String> roleToVegetableMap = Map.of(
                "BACKEND", " 양파",
                "FRONTEND", " 버섯",
                "DESIGN", " 브로콜리",
                "PLANNING", " 당근"
        );
        return roleToVegetableMap.getOrDefault(role, "알 수 없음");
    }

    private String getPotStatus(String  status) {
        if(!status.equals("RECRUITING"))
            return "모집 완료";
        else return "모집 중";
    }

    private String formatDate(java.time.LocalDate date) {
        return (date != null) ? date.format(DATE_FORMATTER) : "N/A";
    }

    private String getKoreanModeOfOperation(String modeOfOperation) {
        Map<String, String> modeOfOperationToKoreanMap = Map.of(
                "ONLINE", "온라인",
                "OFFLINE", "오프라인",
                "HYBRID", "혼합"
        );
        return modeOfOperationToKoreanMap.getOrDefault(modeOfOperation, "알 수 없음");
    }

    private String getKoreanRoleName(String role) {
        Map<String, String> roleToKoreanMap = Map.of(
                "BACKEND", "백엔드",
                "FRONTEND", "프론트엔드",
                "DESIGN", "디자인",
                "PLANNING", "기획"
        );
        return roleToKoreanMap.getOrDefault(role, "알 수 없음");
    }
}
