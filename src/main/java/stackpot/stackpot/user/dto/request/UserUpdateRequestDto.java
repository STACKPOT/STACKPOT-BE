package stackpot.stackpot.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import stackpot.stackpot.Validation.annotation.ValidRole;
import stackpot.stackpot.user.entity.enums.Role;


@Getter
@Setter
@Schema(description = "유저 회원정보 수정 요청 DTO")
public class UserUpdateRequestDto {
    @ValidRole
    @Schema(description = "역할")
    private Role role;

    @Schema(description = "관심사")
    private String interest;

    @Schema(description = "유저 소개")
    private String userIntroduction;

    @Schema(description = "카카오 아이디")
    private String kakaoId;
}
