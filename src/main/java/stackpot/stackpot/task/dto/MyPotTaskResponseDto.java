package stackpot.stackpot.task.dto;

import lombok.*;
import stackpot.stackpot.user.entity.enums.Role;
import stackpot.stackpot.task.entity.enums.TaskboardStatus;

import java.util.List;

@Builder
@Getter
@Setter
public class MyPotTaskResponseDto {
    private Long taskboardId;
    private Long creatorUserId;
    private String creatorNickname;
    private Role creatorRole;
    private String title;
    private String description;
    private String deadLine;
    private TaskboardStatus status;
    private Long potId;
    private String dDay;
    private List<MyPotTaskResponseDto.Participant> participants;


    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Participant {
        private Long potMemberId;
        private Long userId;
        private String nickName;
        private Role role;
    }


}
