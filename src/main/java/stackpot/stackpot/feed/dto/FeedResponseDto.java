package stackpot.stackpot.feed.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import stackpot.stackpot.user.entity.enums.Role;

import java.util.List;
import java.util.Map;

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
    @JsonInclude(JsonInclude.Include.NON_NULL)
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
        private Boolean isOwner;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CreatedFeedDto {
        private Long feedId;
        private Long writerId;
        private String writer;
        private Role writerRole;
        private String title;
        private String content;
        private Long likeCount;
        private Boolean isLiked;
        private String createdAt;
        private List<String> categories;
        private List<String> interests;
        private Map<String, Object> series;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthorizedFeedDto {
        private CreatedFeedDto feed;
        private boolean isOwner;
    }

}
