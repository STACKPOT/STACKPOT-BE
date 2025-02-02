package stackpot.stackpot.web.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.domain.enums.TaskboardStatus;
import stackpot.stackpot.domain.mapping.PotMember;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        private List<Role> category;
        private TaskboardStatus status;
        private String deadLine;
        private List<MyPotTaskResponseDto.Participant> participants;
}
