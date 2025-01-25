package stackpot.stackpot.web.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import stackpot.stackpot.domain.enums.Role;

@Getter
@Setter
@Builder
public class PotRecruitmentResponseDto {
    private Long recruitmentId;
    private Role recruitmentRole;
    private Integer recruitmentCount;

}
