package stackpot.stackpot.pot.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PotMemberRequestDto {
    @ArraySchema(
            arraySchema = @Schema(
                    description = "추가할 지원자 ID 리스트",
                    example = "[1, 2, 3]"
            )
    )
    private List<Long> applicantIds; // 지원자 ID 리스트
}
