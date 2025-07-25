package stackpot.stackpot.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import stackpot.stackpot.user.dto.response.TokenServiceResponse;
import stackpot.stackpot.user.entity.enums.Role;
public class UserResponseDto {
    @Getter
    @Setter
    @Builder
    @Schema(description = "유저 응답 DTO")
    public static class Userdto{
        private Long id;
        private String email; // 이메일
        private String nickname; // 닉네임
        private Role role; // 역할
        private String interest; // 관심사
        private Integer userTemperature; // 유저 온도
        private String kakaoId;
        private String userIntroduction;
        private String userDescription;

    }


    @Getter
    @Setter
    @Builder
    @Schema(description = "유저 로그인 응답 DTO")
    public static class loginDto {
        @Schema(description = "accessToken/refreshToken")
        private TokenServiceResponse tokenServiceResponse;

        @Schema(description = "역할")
        private final Role role;

        @Schema(description = "신규 유저 여부")
        private Boolean isNewUser;
    }
}
