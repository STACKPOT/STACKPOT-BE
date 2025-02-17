package stackpot.stackpot.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class CompletedPotRequestDto {

    @NotBlank(message = "팟 이름은 필수입니다.")
    private String potName;

    private LocalDate potStartDate;

    @NotBlank(message = "사용 언어는 필수입니다.")
    private String potLan;

    private String potSummary;
}
