package stackpot.stackpot.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import stackpot.stackpot.domain.enums.Category;

import java.time.LocalDateTime;
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
        private String nextCursor; // 다음 커서 값
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FeedDto {
        private Long id;
        private String writer;
        private Category category;
        private String title;
        private String content;
        private Long likeCount;
        private String createdAt;
    }
}
