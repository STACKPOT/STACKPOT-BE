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

        if (user.getId() == null) {
            throw new IllegalStateException("User ID is null");
        }
        return UserResponseDto.builder()
                .id(user.getId())  // id 값이 제대로 설정되었는지 로그 확인
                .nickname(user.getNickname())
                .email(user.getEmail())
                .kakaoId(user.getKakaoId())
                .role(user.getRole())
                .interest(user.getInterest())
                .userTemperature(user.getUserTemperature())
                .build();
    }
}

