package stackpot.stackpot.user.converter;

import stackpot.stackpot.user.entity.TempUser;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.dto.response.UserResponseDto;
import stackpot.stackpot.user.dto.response.UserSignUpResponseDto;

import java.util.List;

public class UserConverter {

    public static UserSignUpResponseDto toUserSignUpResponseDto(TempUser user) {
        return UserSignUpResponseDto.builder()
                .id(user.getId())
                .roles(user.getRoleNames())
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
                .roles(user.getRoleNames())
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
                .roles(user.getRoleNames())
                .interest(interests)
                .userTemperature(user.getUserTemperature())
                .userIntroduction(user.getUserIntroduction())
                .build();
    }
}

