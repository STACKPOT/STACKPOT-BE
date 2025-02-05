package stackpot.stackpot.web.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import stackpot.stackpot.domain.enums.Role;

@Getter
@Setter
@Builder
public class UserSignUpResponseDto {
    private Long id;
    private Role role; // 역할
}
