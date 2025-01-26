package stackpot.stackpot.web.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedSearchResponseDto {
    private Long feedId;
    private String title;
    private String content;
    private String creatorNickname;
    private String creatorRole;
    private String createdAt;
    private Long likeCount;
}
