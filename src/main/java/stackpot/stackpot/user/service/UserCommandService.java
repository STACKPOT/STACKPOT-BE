package stackpot.stackpot.user.service;

import stackpot.stackpot.user.dto.request.MyDescriptionRequestDto;
import stackpot.stackpot.user.dto.request.UserRequestDto;
import stackpot.stackpot.user.dto.request.UserUpdateRequestDto;
import stackpot.stackpot.user.dto.response.*;
import stackpot.stackpot.user.entity.enums.Provider;
import stackpot.stackpot.user.entity.enums.Role;

public interface UserCommandService {
    UserSignUpResponseDto joinUser(UserRequestDto.JoinDto request);

    UserResponseDto.loginDto isnewUser(Provider provider, String providerId, String email);

    UserResponseDto.UserInfoDto getMyUsers();
    UserResponseDto.UserInfoDto getUsers(Long UserId);

    UserMyPageResponseDto getMypages();

    UserMyPageResponseDto getUserMypage(Long userId);

    UserResponseDto.Userdto updateUserProfile(UserUpdateRequestDto requestDto);

    NicknameResponseDto createNickname();

    TokenServiceResponse saveNickname(String nickname);

    String deleteUser(String accessToken);

    String logout(String aToken, String refreshToken);

    MyDescriptionResponseDto upsertDescription(MyDescriptionRequestDto dto);
    void deleteDescription();
}
