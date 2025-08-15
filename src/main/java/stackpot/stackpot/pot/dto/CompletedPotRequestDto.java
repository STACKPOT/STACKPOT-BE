package stackpot.stackpot.pot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class CompletedPotRequestDto {

    @NotBlank(message = "팟 이름은 필수입니다.")
    private String potName;

    private String potStartDate;

    @NotBlank(message = "사용 언어는 필수입니다.")
    private String potLan;

    private String potSummary;
}
