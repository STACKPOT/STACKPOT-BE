package stackpot.stackpot.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class PotResponseDto {
    private UserDto user;
    private PotDto pot;
    private List<RecruitmentDetailsDto> recruitmentDetails; // 필드 선언

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
        private String potContent;
        private LocalDate recruitmentDeadline;
        private long dDay;
    }

    @Getter
    @Builder
    public static class RecruitmentDetailsDto {
        private Long recruitmentId;
        private String recruitmentRole;
        private Integer recruitmentCount;
    }
}
