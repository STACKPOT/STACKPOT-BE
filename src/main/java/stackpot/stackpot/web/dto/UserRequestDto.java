package stackpot.stackpot.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import stackpot.stackpot.domain.enums.Role;

public class UserRequestDto {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class JoinDto {
        @Pattern(regexp = "DESIGN|PLANNING|BACKEND|FRONTEND", message = "유효하지 않은 역할입니다.")
        @NotBlank(message = "Role은 공백일 수 없습니다.")
        Role role;
        @NotBlank(message = "Interest는 공백일 수 없습니다.")
        String interest;
        @NotBlank(message = "Nickname은 공백일 수 없습니다.")
        String nickname;
        @NotBlank(message = "KakaoId는 공백일 수 없습니다.")
        String kakaoId;
    }
}
