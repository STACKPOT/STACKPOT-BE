package stackpot.stackpot.pot.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Map;


@Getter
@Setter
@Builder
public class PotDetailResponseDto {
    private Long userId;
    protected String userRole;
    private String userNickname;
    private boolean isOwner;
    private Long potId;
    private String potName;
    private String potStartDate;
    private String potEndDate;
    private String potLan;
    private String potStatus;
    private boolean applied;
    private String potModeOfOperation;
    private String potContent;
    private String potSummary;
    private String dDay;
    private Boolean isSaved;
    private String recruitmentDetails;
    private LocalDate potRecruitmentDeadline;
    private Map<String, Integer> recruitingMembers;
    private Long commentCount;
}