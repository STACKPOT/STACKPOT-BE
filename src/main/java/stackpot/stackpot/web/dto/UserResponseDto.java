package stackpot.stackpot.web.dto;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import stackpot.stackpot.domain.enums.Role;

@Getter
@Setter
@Builder
public class UserResponseDto {
    private String email; // 이메일
    private String nickname; // 닉네임
    private Role role; // 역할
    private String interest; // 관심사
    private Integer userTemperature; // 유저 온도
    private String kakaoId;
    private String userIntroduction;
}
