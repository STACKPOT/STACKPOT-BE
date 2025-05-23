package stackpot.stackpot.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import stackpot.stackpot.user.entity.enums.Role;

@Schema(description = "유저 회원가입 요청 DTO")
public class UserRequestDto {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class JoinDto {
        @Schema(description = "역할")
        Role role;

        @Schema(description = "관심사")
        String interest;

        @Schema(description = "카카오 아이디")
        String kakaoId;
    }
}
