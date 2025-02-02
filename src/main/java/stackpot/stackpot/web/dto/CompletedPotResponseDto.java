package stackpot.stackpot.web.dto;

import lombok.*;
import stackpot.stackpot.domain.enums.Role;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletedPotResponseDto {
    private Long potId; // 팟 ID
    private String potName; // 팟 이름
    private LocalDate potStartDate; // 시작 날짜
    private LocalDate potEndDate; // 종료 날짜
    private String potLan; // 사용 언어
    private String potSummary; // 요약 설명
    private List<RecruitmentDetailResponseDto> recruitmentDetails;
    private Map<String, Integer> roleCounts; // 역할별 인원
    private Role userPotRole;
    private List<BadgeDto> myBadges;
}

