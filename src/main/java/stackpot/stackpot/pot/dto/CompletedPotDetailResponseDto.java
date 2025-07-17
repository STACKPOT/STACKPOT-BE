package stackpot.stackpot.pot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import stackpot.stackpot.badge.dto.BadgeDto;

import java.util.List;

@Getter
@Builder
public class CompletedPotDetailResponseDto {
    private String appealContent;
    private String userPotRole;
    private List<BadgeDto> myBadges;
}

