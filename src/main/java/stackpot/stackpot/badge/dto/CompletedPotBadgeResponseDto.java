package stackpot.stackpot.badge.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletedPotBadgeResponseDto {
    private Long potId; // 팟 ID
    private String potName; // 팟 이름
    private String potStartDate; // 시작 날짜
    private String potEndDate; // 종료 날짜
    private LocalDate potRecruitmentDeadline;
    private String potLan; // 사용 언어
    private String members;
    private String userPotRole;
    private List<BadgeDto> myBadges;
    private Map<String, Integer> memberCounts;

}

