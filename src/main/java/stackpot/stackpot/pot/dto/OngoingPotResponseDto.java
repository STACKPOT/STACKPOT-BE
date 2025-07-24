package stackpot.stackpot.pot.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import stackpot.stackpot.common.util.DateFormatter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class OngoingPotResponseDto {
    private Long potId;
    private String potName;
    private String potStartDate;
    private String potStatus;
    private String potModeOfOperation;
    private String potDuration;
    private String potContent;
    private String dDay;
    private Boolean isOwner;
    private Map<String, Integer> members;
    private Boolean isMember;
}