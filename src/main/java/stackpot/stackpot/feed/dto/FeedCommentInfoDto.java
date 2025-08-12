package stackpot.stackpot.feed.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import stackpot.stackpot.user.entity.enums.Role;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedCommentInfoDto {
    private Long userId;
    private String userName;
    private Long feedWriterId;
    private Long commentId;
    private String comment;
    private Long parentCommentId;
    private LocalDateTime createdAt;
}

