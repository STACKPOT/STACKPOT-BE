package stackpot.stackpot.feed.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import stackpot.stackpot.feed.entity.enums.Category;
import stackpot.stackpot.feed.entity.enums.Interest;

import java.util.List;

public class FeedRequestDto {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class createDto {
        private String title;
        private String content;
        private List<Category> categories;
        private List<Interest> interests;
        private Long seriesId;

    }
}
