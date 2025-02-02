package stackpot.stackpot.converter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.web.dto.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PotDetailConverterImpl implements PotDetailConverter {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    @Override
    public CompletedPotDetailResponseDto toCompletedPotDetailDto(Pot pot, String appealContent) {
        // 날짜 포맷 설정
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy. M. d");

        return CompletedPotDetailResponseDto.builder()
                .potId(pot.getPotId())
                .potName(pot.getPotName())
                .potStartDate(pot.getPotStartDate() != null ? pot.getPotStartDate().format(formatter) : null)
                .potEndDate(pot.getPotEndDate() != null ? pot.getPotEndDate().format(formatter) : null)
                .potContent(pot.getPotContent())
                .potStatus(pot.getPotStatus())
                .potSummary(pot.getPotSummary())
                .appealContent(appealContent)
                .build();
    }

    @Override
    public PotDetailResponseDto toPotDetailResponseDto(User user, Pot pot, String recruitmentDetails){
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

        return PotDetailResponseDto.builder()
                .userId(user.getId())
                .userRole(String.valueOf(user.getRole()))
                .userNickname(user.getNickname() + getVegetableNameByRole(user.getRole().name()))
                .potId(pot.getPotId())
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
}
