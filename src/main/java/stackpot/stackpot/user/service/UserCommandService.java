package stackpot.stackpot.user.service;

import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.entity.enums.Role;
import stackpot.stackpot.user.dto.*;

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
