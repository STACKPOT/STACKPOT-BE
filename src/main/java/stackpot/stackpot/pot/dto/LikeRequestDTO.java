package stackpot.stackpot.pot.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class LikeRequestDTO {
    private Long applicationId;

    @Builder.Default
    private Boolean liked = false;
}
