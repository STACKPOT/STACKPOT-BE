package stackpot.stackpot.feed.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import stackpot.stackpot.feed.entity.enums.Category;

public class FeedRequestDto {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class createDto {
        private String title;
        private String content;
        private Category category;
    }
}
