package stackpot.stackpot.web.dto;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Null;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import stackpot.stackpot.domain.enums.Role;
public class UserResponseDto {
    @Getter
    @Setter
    @Builder
    public static class Userdto{
        private Long id;
        private String email; // 이메일
        private String nickname; // 닉네임
        private Role role; // 역할
        private String interest; // 관심사
        private Integer userTemperature; // 유저 온도
        private String kakaoId;
        private String userIntroduction;

    }


    @Getter
    @Setter
    @Builder
    public static class loginDto {
        private TokenServiceResponse tokenServiceResponse;
        private final Role role;
        private Boolean isNewUser;
    }
}
