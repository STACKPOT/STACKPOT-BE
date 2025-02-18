package stackpot.stackpot.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class MonthlyTaskDto {
    private Long taskId;
    private LocalDate deadLine;
    private boolean isParticipating;
}
