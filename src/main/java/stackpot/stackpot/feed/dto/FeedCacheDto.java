package stackpot.stackpot.feed.dto;

import lombok.*;
import stackpot.stackpot.user.entity.enums.Role;

import java.time.LocalDateTime;
import java.util.List;

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
    private List<String> writerRoles;
    private String createdAt;
}


