package stackpot.stackpot.user.converter;

import stackpot.stackpot.user.entity.Interest;
import stackpot.stackpot.user.entity.TempUser;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.entity.enums.Role;
import stackpot.stackpot.user.dto.request.UserRequestDto;
import stackpot.stackpot.user.dto.response.UserResponseDto;
import stackpot.stackpot.user.dto.response.UserSignUpResponseDto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UserConverter {
    public static User toUser(UserRequestDto.JoinDto request) {
        List<String> interests = request.getInterest();

        return User.builder()
                .interests(interests)
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

        String nicknameWithRole = user.getNickname() + " 새싹";

        List<String> interests = user.getInterests();

        return UserResponseDto.Userdto.builder()
                .id(user.getId())
                .nickname(nicknameWithRole)
                .email(user.getEmail())
                .kakaoId(user.getKakaoId())
                .role(user.getRole())
                .interest(interests)
                .userTemperature(user.getUserTemperature())
                .userDescription(user.getUserDescription())
                .userIntroduction(user.getUserIntroduction())
                .build();
    }

    public static UserResponseDto.UserInfoDto toUserInfo(User user) {

        List<String> interests = user.getInterests();

        if (user.getId() == null) {
            throw new IllegalStateException("User ID is null");
        }

        String nicknameWithRole = user.getNickname() + " 새싹";

        return UserResponseDto.UserInfoDto.builder()
                .id(user.getId())
                .nickname(nicknameWithRole)
                .role(user.getRole())
                .interest(interests)
                .userTemperature(user.getUserTemperature())
                .userIntroduction(user.getUserIntroduction())
                .build();
    }
}

