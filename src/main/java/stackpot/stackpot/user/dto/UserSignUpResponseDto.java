package stackpot.stackpot.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import stackpot.stackpot.user.entity.enums.Role;

@Getter
@Setter
@Builder
public class UserSignUpResponseDto {
    private Long id;
    private Role role; // 역할
}
