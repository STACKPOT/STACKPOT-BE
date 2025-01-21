package stackpot.stackpot.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

public class UserRequestDto {

    @Getter
    @Setter
    public static class JoinDto {
        @NotBlank(message = "Role은 공백일 수 없습니다.")
        String role;

        @NotBlank(message = "Interest는 공백일 수 없습니다.")
        String interest;

        @NotBlank(message = "Nickname은 공백일 수 없습니다.")
        String nickname;

        @NotBlank(message = "KakaoId는 공백일 수 없습니다.")
        String kakaoId;

    }
}
