package stackpot.stackpot.pot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import stackpot.stackpot.user.entity.enums.Role;

import java.time.LocalDateTime;
import java.util.List;

public class PotCommentResponseDto {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllPotCommentDto {
        private Long userId;
        private String userName;
        private Boolean isCommentWriter;
        private Boolean isPotWriter;
        private Long commentId;
        private String comment;
        private Long parentCommentId;
        private String createdAt;
        private List<AllPotCommentDto> children;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PotCommentCreateDto {
        private Long userId;
        private String userName;
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
        private Long userId;
        private String userName;
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
