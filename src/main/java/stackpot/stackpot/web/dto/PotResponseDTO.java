package stackpot.stackpot.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class PotResponseDTO {
        private long potId;
        private String potName;
        private LocalDate potStartDate;
        private LocalDate potEndDate;
        private String potDuration;
        private String potLan;
        private String potContent;
        private String potStatus;
        private String potSummary;
        private LocalDate recruitmentDeadline;
        private String potModeOfOperation;
        private Integer dDay;

}
