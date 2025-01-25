package stackpot.stackpot.web.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import stackpot.stackpot.domain.Pot;

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
    private String potDuration;
    private String potLan;
    private String potContent;
    private String potStatus;
    private String potModeOfOperation;
    private String potSummary;
    private LocalDate recruitmentDeadline;
    private List<PotRecruitmentResponseDto> recruitmentDetails;
}
