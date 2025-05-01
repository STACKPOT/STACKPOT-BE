package stackpot.stackpot.task.dto;

import lombok.*;
import stackpot.stackpot.task.entity.enums.TaskboardStatus;

@Builder
@Getter
@Setter
public class MyPotTaskStatusResponseDto {
    private Long taskboardId;
    private String title;
    private TaskboardStatus status;

}
