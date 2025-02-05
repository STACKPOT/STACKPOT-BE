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
    private String email; // 이메일
    private Role role; // 역할
    private String interest; // 관심사
    private String kakaoId;
}
