package stackpot.stackpot.pot.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import stackpot.stackpot.user.entity.enums.Role;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class PotResponseDto {
    private Long potId;
    private String potName;
    private String potStartDate;
    private String potEndDate;
    private LocalDate potRecruitmentDeadline;
    private String potLan;
    private String potContent;
    private String potStatus;
    private String potModeOfOperation;
    private String potSummary;
    private List<PotRecruitmentResponseDto> recruitmentDetails;
}
