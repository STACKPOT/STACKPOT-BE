package stackpot.stackpot.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)  // null 필드는 JSON 응답에서 제외
public class PotResponseDTO {
    private UserDto user;
    private PotDto pot;
    private List<RecruitmentDetailsDto> recruitmentDetails = null;
    private List<ApplicantDto> applicants = null;


    @Getter
    @Builder
    public static class UserDto {
        private String nickname;
        private String role;
    }

    @Getter
    @Builder
    public static class PotDto {
        private long potId;
        private String potName;
        private LocalDate potStartDate;
        private LocalDate potEndDate;
        private String potDuration;
        private String potLan;
        private String potContent;
        private String potStatus;
        private String potSummary;
        private LocalDate recruitmentDeadline;
        private String potModeOfOperation;
        private Integer dDay;
    }

    @Getter
    @Builder
    public static class RecruitmentDetailsDto {
        private Long recruitmentId;
        private String recruitmentRole;
        private Integer recruitmentCount;
    }

    @Getter
    @Builder
    public static class ApplicantDto {
        private Long applicationId;
        private String potRole;
        private Boolean liked;
    }
}
