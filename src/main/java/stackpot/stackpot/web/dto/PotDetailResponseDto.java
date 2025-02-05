package stackpot.stackpot.web.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;


@Getter
@Setter
@Builder
@JsonPropertyOrder({
        "userId",
        "userRole",
        "userNickname",
        "isOwner",
        "potId",
        "potName",
        "potStartDate",
        "potDuration",
        "potLan",
        "potModeOfOperation",
        "potContent",
        "dDay",
        "recruitmentDetails"
})
public class PotDetailResponseDto {
    private Long userId;
    protected String userRole;
    private String userNickname;
    private boolean isOwner;
    private Long potId;
    private String potName;
    private String potStartDate;
    private String potDuration;
    private String potLan;
    private String potModeOfOperation;
    private String potContent;
    private String dDay;
    private String recruitmentDetails;
}