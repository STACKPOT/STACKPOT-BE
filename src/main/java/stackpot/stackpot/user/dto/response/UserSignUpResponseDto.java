package stackpot.stackpot.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import stackpot.stackpot.user.entity.enums.Role;

import java.util.List;

@Getter
@Setter
@Builder
@Schema(description = "유저 회원가입 응답 DTO")
public class UserSignUpResponseDto {
    @Schema(description = "유저 아이디")
    private Long id;

    @Schema(description = "역할")
    private List<String> roles;
}
