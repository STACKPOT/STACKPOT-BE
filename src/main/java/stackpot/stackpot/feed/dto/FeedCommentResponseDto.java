package stackpot.stackpot.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import stackpot.stackpot.user.entity.enums.Role;

import java.time.LocalDateTime;
import java.util.List;

public class FeedCommentResponseDto {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllFeedCommentDto {
        private Long userId;
        private String userName;
        private Role role;
        private Boolean isWriter;
        private Long commentId;
        private String comment;
        private Long parentCommentId;
        private LocalDateTime createdAt;
        private List<AllFeedCommentDto> children;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeedCommentCreateDto {
        private Long userId;
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
    public static class FeedReplyCommentCreateDto {
        private Long userId;
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
    public static class FeedCommentUpdateDto {
        private String comment;
    }
}
