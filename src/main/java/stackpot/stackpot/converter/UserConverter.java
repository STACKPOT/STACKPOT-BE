package stackpot.stackpot.converter;

import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.web.dto.UserRequestDto;
import stackpot.stackpot.web.dto.UserResponseDto;

public class UserConverter {
    public static User toUser(UserRequestDto.JoinDto request) {

        return User.builder()
                .nickname(request.getNickname())
                .kakaoId(request.getKakaoId())
                .interest(request.getInterest())
                .role(Role.valueOf(String.valueOf(request.getRole())))
                .build();
    }

    public static UserResponseDto toDto(User user) {

        return UserResponseDto.builder()
                .nickname(user.getNickname())
                .email(user.getEmail())   // 추가된 코드
                .kakaoId(user.getKakaoId())
                .role(user.getRole())
                .interest(user.getInterest())
                .userTemperature(user.getUserTemperature())
                .build();
    }
}

