package stackpot.stackpot.web.dto;

import lombok.*;
import stackpot.stackpot.domain.enums.Role;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletedPotBadgeResponseDto {
    private Long potId; // 팟 ID
    private String potName; // 팟 이름
    private LocalDate potStartDate; // 시작 날짜
    private LocalDate potEndDate; // 종료 날짜
    private String potLan; // 사용 언어
    private String members;
    private Role userPotRole;
    private List<BadgeDto> myBadges;
}

