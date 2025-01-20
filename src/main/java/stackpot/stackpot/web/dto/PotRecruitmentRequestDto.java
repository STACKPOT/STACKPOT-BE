package stackpot.stackpot.web.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PotRecruitmentRequestDto {
    private String recruitmentRole;
    private Integer recruitmentCount;
}
