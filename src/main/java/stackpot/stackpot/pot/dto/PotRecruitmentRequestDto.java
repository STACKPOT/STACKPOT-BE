package stackpot.stackpot.pot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import stackpot.stackpot.Validation.annotation.ValidRole;

@Getter
@Setter
@Builder
public class PotRecruitmentRequestDto {
    @ValidRole
    @Schema(description = "모집 역할", example = "BACKEND")
    private String recruitmentRole;
    @Schema(description = "역할 별 모집 인원 수", example = "1")
    private Integer recruitmentCount;
}
