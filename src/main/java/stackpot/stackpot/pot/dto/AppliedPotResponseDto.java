package stackpot.stackpot.pot.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
@JsonPropertyOrder({
        "userId",
        "userRole",
        "userNickname",
        "potId",
        "potStatus",
        "potName",
        "potStartDate",
        "potDuration",
        "potLan",
        "potModeOfOperation",
        "potContent",
        "dDay",
        "recruitmentDetails"
})
public class AppliedPotResponseDto {
    private Long userId;
    protected String userRole;
    private String userNickname;
    private Long potId;
    private String potStatus;
    private String potName;
    private String potStartDate;
    private String potDuration;
    private String potLan;
    private String potModeOfOperation;
    private String potContent;
    private String dDay;
    private String recruitmentDetails;
}