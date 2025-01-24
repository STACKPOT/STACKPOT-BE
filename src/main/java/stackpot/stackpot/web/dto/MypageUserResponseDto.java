package stackpot.stackpot.web.dto;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import stackpot.stackpot.domain.enums.Role;

@Getter
@Setter
@Builder
public class MypageUserResponseDto {

    private UserResponseDto user;
    private String userIntroduction; // 한 줄 소개
}
