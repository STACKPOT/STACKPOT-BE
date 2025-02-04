package stackpot.stackpot.web.dto;

import lombok.*;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.domain.enums.TaskboardStatus;

import java.util.List;

@Builder
@Getter
@Setter
public class MyPotTaskStatusResponseDto {
    private Long taskboardId;
    private String title;
    private TaskboardStatus status;

}
