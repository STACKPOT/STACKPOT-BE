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
    private String potStartDate; // 시작 날짜
    private String potEndDate; // 종료 날짜
    private String potLan; // 사용 언어
    private String members;
    private String userPotRole;
    private Map<String, Integer> memberCounts;
}

