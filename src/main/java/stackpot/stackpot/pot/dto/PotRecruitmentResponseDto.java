package stackpot.stackpot.pot.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PotRecruitmentResponseDto {
    private String recruitmentRole;
    private Integer recruitmentCount;
}
