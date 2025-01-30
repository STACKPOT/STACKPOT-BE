package stackpot.stackpot.converter;

import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.web.dto.UserRequestDto;
import stackpot.stackpot.web.dto.UserResponseDto;

import java.util.Map;

public class UserConverter {
    public static User toUser(UserRequestDto.JoinDto request) {

        return User.builder()
                .nickname(request.getNickname())
                .kakaoId(request.getKakaoId())
                .interest(request.getInterest())
                .role(Role.valueOf(String.valueOf(request.getRole())))
                .build();
    }

    public static UserResponseDto.Userdto toDto(User user) {

        if (user.getId() == null) {
            throw new IllegalStateException("User ID is null");
        }

        // 역할명을 변환하여 닉네임에 추가
        String roleName = user.getRole() != null ? user.getRole().name() : "멤버";
        String nicknameWithRole = user.getNickname() + " " + toDtoRole(roleName);

        return UserResponseDto.Userdto.builder()
                .id(user.getId())  // id 값이 제대로 설정되었는지 로그 확인
                .nickname(nicknameWithRole)
                .email(user.getEmail())
                .kakaoId(user.getKakaoId())
                .role(user.getRole())
                .interest(user.getInterest())
                .userTemperature(user.getUserTemperature())
                .build();
    }

    public static String toDtoRole(String roleName) {
        return switch (roleName) {
            case "BACKEND" -> "양파";
            case "FRONTEND" -> "버섯";
            case "DESIGN" -> "브로콜리";
            case "PLANNING" -> "당근";
            default -> "멤버";
        };
    }
}

