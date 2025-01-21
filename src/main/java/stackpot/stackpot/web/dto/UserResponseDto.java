package stackpot.stackpot.web.dto;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserResponseDto {
    private String email; // 이메일
    private String nickname; // 닉네임
    private String role; // 역할
    private String interest; // 관심사
    private Integer userTemperature; // 유저 온도
    private String kakaoId;
}
