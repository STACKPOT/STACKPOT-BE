package stackpot.stackpot.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import stackpot.stackpot.user.entity.enums.Role;

import java.util.List;
import java.util.Set;

@Schema(description = "유저 회원가입 요청 DTO")
public class UserRequestDto {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class JoinDto {
        @Schema(description = "역할")
        private List<Role> roles;

        @Schema(description = "관심사", example = "[\"사이드 프로젝트\", \"1인 개발\"]")
        List<String> interest;
    }
}
