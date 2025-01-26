package stackpot.stackpot.web.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import stackpot.stackpot.Validation.annotation.ValidRole;
import stackpot.stackpot.domain.enums.Role;


@Getter
@Setter
public class UserUpdateRequestDto {
    @ValidRole
    private Role role;
    private String interest;
    private String userIntroduction;
}
