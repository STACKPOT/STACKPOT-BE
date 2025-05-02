package stackpot.stackpot.user.converter;

import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.entity.enums.Role;
import stackpot.stackpot.user.dto.UserRequestDto;
import stackpot.stackpot.user.dto.UserResponseDto;
import stackpot.stackpot.user.dto.UserSignUpResponseDto;

public class UserConverter {
    public static User toUser(UserRequestDto.JoinDto request) {

        return User.builder()
                .kakaoId(request.getKakaoId())
                .interest(request.getInterest())
                .role(Role.valueOf(String.valueOf(request.getRole())))
                .build();
    }

    public static UserSignUpResponseDto toUserSignUpResponseDto(User user) {
        return UserSignUpResponseDto.builder()
                .id(user.getId())
                .role(user.getRole())
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
                .id(user.getId())
                .nickname(nicknameWithRole)
                .email(user.getEmail())
                .kakaoId(user.getKakaoId())
                .role(user.getRole())
                .interest(user.getInterest())
                .userTemperature(user.getUserTemperature())
                .userIntroduction(user.getUserIntroduction())
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

