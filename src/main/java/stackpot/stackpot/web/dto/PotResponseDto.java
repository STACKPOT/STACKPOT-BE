package stackpot.stackpot.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class PotResponseDto {
    private UserDto user;
    private PotDto pot;

    @Getter
    @Builder
    public static class UserDto {
        private String nickname;
        private String role;
    }

    @Getter
    @Builder
    public static class PotDto {
        private String potName;
        private String potContent;
        private LocalDate recruitmentDeadline;
        private long dDay;
    }
}
