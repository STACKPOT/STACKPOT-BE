package stackpot.stackpot.pot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import stackpot.stackpot.user.entity.enums.Role;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class PotRequestDto {

    @NotBlank(message = "팟 이름은 필수입니다.")
    private String potName;

    private LocalDate potStartDate;

//    private LocalDate potEndDate;
    private Role potRole;

    @NotBlank(message = "예상 기간은 필수입니다.")
    private String potDuration;

    @NotBlank(message = "사용 언어는 필수입니다.")
    private String potLan;

    private String potContent;

//    @NotBlank(message = "팟 상태는 필수입니다.")
//    private String potStatus;

    private String potModeOfOperation;

    private String potSummary;

    private LocalDate recruitmentDeadline;

    private List<PotRecruitmentRequestDto> recruitmentDetails;
}
