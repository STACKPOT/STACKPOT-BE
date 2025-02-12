package stackpot.stackpot.web.dto;

import lombok.*;
import stackpot.stackpot.domain.enums.Category;
import stackpot.stackpot.domain.enums.Role;

import java.util.List;

public class FeedResponseDto {

    @Data
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FeedPreviewList {
        private List<FeedDto> feeds;
        private Long nextCursor; // 다음 커서 값
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FeedDto {
        private Long feedId;
        private Long writerId;
        private String writer;
        private Role writerRole;
        private String title;
        private String content;
        private Long likeCount;
        private Boolean isLiked;
        private String createdAt;
    }
}
