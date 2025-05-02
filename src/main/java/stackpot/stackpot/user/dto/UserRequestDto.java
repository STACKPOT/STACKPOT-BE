package stackpot.stackpot.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import stackpot.stackpot.user.entity.enums.Role;

public class UserRequestDto {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class JoinDto {
        Role role;
//        @NotBlank(message = "Interest는 공백일 수 없습니다.")
        String interest;
//        @NotBlank(message = "KakaoId는 공백일 수 없습니다.")
        String kakaoId;
    }
}
