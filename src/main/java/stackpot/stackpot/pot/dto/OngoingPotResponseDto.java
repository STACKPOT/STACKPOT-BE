package stackpot.stackpot.pot.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class OngoingPotResponseDto {
    private Long potId;
    private String potName;
    private String potStatus;
    private Boolean isOwner;
    private Map<String, Integer> members;

}

