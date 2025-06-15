package stackpot.stackpot.pot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import stackpot.stackpot.user.entity.enums.Role;

import java.time.LocalDateTime;

public class PotCommentResponseDto {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PotCommentCreateDto {
        private String userName;
        private Role role;
        private Boolean isWriter;
        private Long commentId;
        private String comment;
        private LocalDateTime createdAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PotReplyCommentCreateDto {
        private String userName;
        private Role role;
        private Boolean isWriter;
        private Long commentId;
        private String comment;
        private Long parentCommentId;
        private LocalDateTime createdAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllPotCommentDto {
        private String userName;
        private Role role;
        private Boolean isWriter;
        private Long commentId;
        private String comment;
        private Long parentCommentId;
        private LocalDateTime createdAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PotCommentUpdateDto {
        private String comment;
    }
}
