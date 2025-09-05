package stackpot.stackpot.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import stackpot.stackpot.user.entity.enums.Role;

import java.util.List;
import java.util.Set;

public class UserResponseDto {
    @Getter
    @Setter
    @Builder
    @Schema(description = "유저 응답 DTO")
    public static class Userdto{
        private Long id;
        private String email; // 이메일
        private String nickname; // 닉네임
        private List<String> roles; // 역할
        @Schema(description = "관심사", example = "[\"사이드 프로젝트\", \"1인 개발\"]")
        private List<String> interest; // 관심사
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
        private List<String> roles;

        @Schema(description = "신규 유저 여부")
        private Boolean isNewUser;
    }

    @Getter
    @Setter
    @Builder
    @Schema(description = "사용자 정보 조회 DTO")
    public static class UserInfoDto {
        private Long id;
        private String nickname;
        private List<String> roles;
        private List<String> interest; // 관심사
        private Integer userTemperature;
        private String userIntroduction;
    }
}
