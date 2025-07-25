package stackpot.stackpot.feed.dto;

import lombok.*;
import stackpot.stackpot.user.entity.enums.Role;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedCacheDto {
    private Long feedId;
    private String title;
    private String content;
    private Long userId;
    private String writer;
    private Role writerRole;
    private String createdAt;
}


