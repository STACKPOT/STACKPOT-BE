package stackpot.stackpot.feed.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import stackpot.stackpot.user.entity.enums.Role;

import java.util.Collections;
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
        // [category, series X / like, save O]일 때사용하는 DTO:  피드 전체 조회, 사용자별, 나의 피드 조회, 검색
        public static FeedPreviewList empty() {
            return FeedPreviewList.builder()
                    .feeds(Collections.emptyList())
                    .nextCursor(null)
                    .build();
        }

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
        private List<String> writerRoles;
        private String title;
        private String content;
        private Long likeCount;
        private Long commentCount;
        private Long saveCount;
        private Boolean isLiked;
        private Boolean isSaved;
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
        private List<String> writerRoles;
        private String title;
        private String content;
        private String createdAt;
        private List<String> categories;
        private List<String> interests;
        private Map<String, Object> series;
        // save, like 정보가 필요 없는 경우 사용하는 DTO: 피드 생성, 피드 수정
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthorizedFeedDto {
        private CreatedFeedDto feed;
        private boolean isOwner;
        private Boolean isLiked;
        private Boolean isSaved;
        private Long commentCount;
        // category, series, like, save까지 싹 다 필요한 경우 사용하는 DTO: 피드 상세 조회
    }

}
