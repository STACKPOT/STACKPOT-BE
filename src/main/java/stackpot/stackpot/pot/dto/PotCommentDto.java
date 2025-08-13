package stackpot.stackpot.pot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import stackpot.stackpot.user.entity.enums.Role;

import java.time.LocalDateTime;

public class PotCommentDto {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PotCommentInfoDto {
        private Long userId;
        private String userName;
        private Long potWriterId; // Pot 작성자
        private Long commentId;
        private String comment;
        private Long parentCommentId;
        private LocalDateTime createdAt;
    }
}
