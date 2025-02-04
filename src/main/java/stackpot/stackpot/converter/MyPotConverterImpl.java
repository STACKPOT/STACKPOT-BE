package stackpot.stackpot.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.web.dto.BadgeDto;
import stackpot.stackpot.web.dto.CompletedPotBadgeResponseDto;
import stackpot.stackpot.web.dto.OngoingPotResponseDto;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class MyPotConverterImpl implements MyPotConverter{

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy. MM. dd");
    private String formatDate(java.time.LocalDate date) {
        return (date != null) ? date.format(DATE_FORMATTER) : "N/A";
    }


    public OngoingPotResponseDto convertToOngoingPotResponseDto(Pot pot, Long userId) {
        // Role별 인원 수 집계
        Map<String, Integer> roleCountMap = pot.getPotMembers().stream()
                .collect(Collectors.groupingBy(
                        member -> member.getRoleName().name(), // Role Enum을 문자열로 변환
                        Collectors.reducing(0, e -> 1, Integer::sum) // 각 역할의 개수를 세기
                ));

        return OngoingPotResponseDto.builder()
                .potId(pot.getPotId())
                .potName(pot.getPotName())
                .potStatus(pot.getPotStatus())  // 진행 중인 팟 상태
                .members(roleCountMap)// 역할 개수 Map 적용
                .isOwner(isOwnerCheck(userId, pot))
                .build();
    }

    private Boolean isOwnerCheck(Long userId, Pot pot) {
        if(userId == pot.getUser().getId())
            return true;
        else return false;
    }

    @Override
    public CompletedPotBadgeResponseDto toCompletedPotBadgeResponseDto(
            Pot pot, String formattedMembers, Role userPotRole, List<BadgeDto> myBadges) {
        return CompletedPotBadgeResponseDto.builder()
                .potId(pot.getPotId())
                .potName(pot.getPotName())
                .potStartDate(formatDate(pot.getPotStartDate()))
                .potEndDate(formatDate(pot.getPotEndDate()))
                .potLan(pot.getPotLan())
                .members(formattedMembers)  //  "프론트엔드(2), 백엔드(1)" 형식 적용
                .userPotRole(getKoreanRoleName(String.valueOf(userPotRole)))
                .myBadges(myBadges) //  사용자의 뱃지 포함
                .build();
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
