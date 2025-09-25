package stackpot.stackpot.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import stackpot.stackpot.Validation.annotation.ValidRole;
import stackpot.stackpot.user.entity.enums.Role;

import java.util.List;
import java.util.Set;



@Getter
@Setter
@Schema(description = "유저 회원정보 수정 요청 DTO")
public class UserUpdateRequestDto {
    @ValidRole
    @Schema(description = "역할")
    private List<Role> roles;

    @Schema(description = "관심사", example = "[\"사이드 프로젝트\", \"1인 개발\"]")
    private List<String> interest;

    @Schema(description = "유저 소개")
    private String userIntroduction;
}
