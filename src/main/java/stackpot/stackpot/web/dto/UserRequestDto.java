package stackpot.stackpot.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

        Role role;
        @NotBlank(message = "Interest는 공백일 수 없습니다.")
        String interest;
        @NotBlank(message = "Nickname은 공백일 수 없습니다.")
        String nickname;
        @NotBlank(message = "KakaoId는 공백일 수 없습니다.")
        String kakaoId;
    }
}
