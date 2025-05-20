package stackpot.stackpot.user.converter;

import stackpot.stackpot.user.entity.TempUser;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.entity.enums.Role;
import stackpot.stackpot.user.dto.request.UserRequestDto;
import stackpot.stackpot.user.dto.response.UserResponseDto;
import stackpot.stackpot.user.dto.response.UserSignUpResponseDto;

public class UserConverter {
    public static User toUser(UserRequestDto.JoinDto request) {

        return User.builder()
                .kakaoId(request.getKakaoId())
                .interest(request.getInterest())
                .role(Role.valueOf(String.valueOf(request.getRole())))
                .build();
    }

    public static UserSignUpResponseDto toUserSignUpResponseDto(TempUser user) {
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
        String nicknameWithRole = user.getNickname() + " " + Role.toVegetable(roleName);

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
}

