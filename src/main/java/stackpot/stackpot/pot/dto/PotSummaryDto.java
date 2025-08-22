package stackpot.stackpot.pot.dto;

import lombok.Builder;
import lombok.Getter;
import stackpot.stackpot.badge.dto.BadgeDto;

import java.util.List;

@Getter
@Builder
public class PotSummaryDto {
    private String summary;
    private List<String> potLan;
    private String potName;
    private Boolean isMember;
}

