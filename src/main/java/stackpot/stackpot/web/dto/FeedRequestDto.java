package stackpot.stackpot.web.dto;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import stackpot.stackpot.domain.enums.Visibility;

public class FeedRequestDto {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class createDto {
        private String title;
        private String content;
        private Visibility visibility;
    }
}
