package stackpot.stackpot.user.dto;

import lombok.Getter;
import lombok.Setter;
import stackpot.stackpot.Validation.annotation.ValidRole;
import stackpot.stackpot.user.entity.enums.Role;


@Getter
@Setter
public class UserUpdateRequestDto {
    @ValidRole
    private Role role;
    private String interest;
    private String userIntroduction;
    private String kakaoId;
}
