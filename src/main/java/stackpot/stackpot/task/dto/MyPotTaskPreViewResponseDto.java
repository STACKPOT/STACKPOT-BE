package stackpot.stackpot.task.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import stackpot.stackpot.user.entity.enums.Role;
import stackpot.stackpot.task.entity.enums.TaskboardStatus;

import java.util.List;

@Getter
@Setter
@Builder
public class MyPotTaskPreViewResponseDto {
        private Long taskboardId;
        private String title;
        private String description;
        private String creatorNickname;
        private Role creatorRole;
        private String dDay;
        private List<Role> category;
        private TaskboardStatus status;
        private String deadLine;
        private List<MyPotTaskResponseDto.Participant> participants;
}
