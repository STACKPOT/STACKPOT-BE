package stackpot.stackpot.user.service;

import stackpot.stackpot.user.dto.request.UserRequestDto;
import stackpot.stackpot.user.dto.request.UserUpdateRequestDto;
import stackpot.stackpot.user.dto.response.NicknameResponseDto;
import stackpot.stackpot.user.dto.response.UserMyPageResponseDto;
import stackpot.stackpot.user.dto.response.UserResponseDto;
import stackpot.stackpot.user.dto.response.UserSignUpResponseDto;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.entity.enums.Role;

public interface UserCommandService {
    UserSignUpResponseDto joinUser(UserRequestDto.JoinDto request);
    User saveNewUser(String email, UserRequestDto.JoinDto request);

    UserResponseDto.loginDto isnewUser(String email);

    UserResponseDto.Userdto getMyUsers();
    UserResponseDto.Userdto getUsers(Long UserId);

    UserMyPageResponseDto getMypages(String dataType);

    UserMyPageResponseDto getUserMypage(Long userId, String dataType);

    UserResponseDto.Userdto updateUserProfile(UserUpdateRequestDto requestDto);

    NicknameResponseDto createNickname(Role role);

    String saveNickname(String nickname);

    String deleteUser(String accessToken);

    String logout(String aToken, String refreshToken);
}
