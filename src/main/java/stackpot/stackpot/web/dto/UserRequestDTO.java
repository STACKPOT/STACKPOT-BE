package stackpot.stackpot.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import javax.management.relation.Role;

public class UserRequestDTO {

    @Getter
    @Setter
    public static class JoinDto {
        @NotNull
        Role role;
        @NotNull
        String interest;
        @NotBlank
        String nickname;
        @NotBlank
        @Email
        String email; // 이메일
        @NotNull
        String kakaoId; // 카카오 아이디

    }
}
