package stackpot.stackpot.web.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedSearchResponseDto {
    private Long feedId;
    private Long userId;

    private String creatorRole;
    private Boolean isLiked;
    private String title;
    private String content;
    private String creatorNickname;
    private String createdAt;
    private Long likeCount;
}
