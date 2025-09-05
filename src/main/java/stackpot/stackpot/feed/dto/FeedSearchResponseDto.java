package stackpot.stackpot.feed.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedSearchResponseDto {
    private Long feedId;
    private Long userId;

    private List<String> creatorRole;
    private Boolean isLiked;
    private String title;
    private String content;
    private String creatorNickname;
    private String createdAt;
    private Long likeCount;
}
