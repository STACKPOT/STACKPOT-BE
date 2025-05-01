package stackpot.stackpot.pot.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class RecruitingPotResponseDto {
    private Long potId;
    private String potName;
    private Map<String, Integer> members;
    private String dDay;
}

