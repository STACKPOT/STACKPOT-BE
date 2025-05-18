package stackpot.stackpot.pot.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import stackpot.stackpot.Validation.annotation.ValidRole;

@Getter
@Setter
@Builder
public class PotRecruitmentRequestDto {
    @ValidRole
    private String recruitmentRole;
    private Integer recruitmentCount;
}
