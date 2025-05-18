package stackpot.stackpot.badge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BadgeDto {
    private Long badgeId;
    private String badgeName;
}
