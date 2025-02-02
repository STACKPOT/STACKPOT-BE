package stackpot.stackpot.web.dto;

import jakarta.persistence.*;
import lombok.*;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.domain.enums.TaskboardStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
public class MyPotTaskResponseDto {
    private Long taskboardId;
    private String creatorNickname;
    private Role creatorRole;
    private String title;
    private String description;
    private String deadLine;
    private TaskboardStatus status;
    private Long potId;
    private String dDay;
    private List<Participant> participants;


    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Participant {
        private Long potMemberId;
        private String nickName;
        private Role role;
    }


}
