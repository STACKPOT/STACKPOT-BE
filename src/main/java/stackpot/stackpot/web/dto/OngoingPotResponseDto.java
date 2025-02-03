package stackpot.stackpot.web.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import stackpot.stackpot.domain.enums.Role;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class OngoingPotResponseDto {
    private Long potId;
    private String potName;
    private String potStatus;
    private Map<String, Integer> members;

}

