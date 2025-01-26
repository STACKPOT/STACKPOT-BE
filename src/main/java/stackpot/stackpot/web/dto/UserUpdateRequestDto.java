package stackpot.stackpot.web.dto;

import lombok.Getter;
import lombok.Setter;
import stackpot.stackpot.domain.enums.Role;


@Getter
@Setter
public class UserUpdateRequestDto {
    private Role role;
    private String interest;
    private String userIntroduction;
}
