package stackpot.stackpot.pot.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class AppliedPotResponseDto {
    private Long potId;
    private String potName;
    private String potStartDate;
    private String potEndDate;
    private LocalDate potRecruitmentDeadline;
    private String potStatus;
    private String potModeOfOperation;
    private String potContent;
    private String dDay;
    private List<String> recruitmentRoles;
    private Map<String, Integer> members;
}