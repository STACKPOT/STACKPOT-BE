package stackpot.stackpot.pot.dto;

import lombok.Builder;
import lombok.Getter;
import stackpot.stackpot.badge.dto.BadgeDto;

import java.util.List;

@Getter
@Builder
public class AppealContentDto {
    private String appealContent;
    private String userPotRole;
    private List<BadgeDto> myBadges;
}

