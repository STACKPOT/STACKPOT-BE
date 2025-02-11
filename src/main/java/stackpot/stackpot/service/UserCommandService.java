package stackpot.stackpot.service;

import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.web.dto.*;

public interface UserCommandService {
    UserSignUpResponseDto joinUser(UserRequestDto.JoinDto request);
    User saveNewUser(String email, UserRequestDto.JoinDto request);

    UserResponseDto.loginDto isnewUser(String email);

    UserResponseDto.Userdto getMyUsers();
    UserResponseDto.Userdto getUsers(Long UserId);

    UserMyPageResponseDto getMypages(String dataType);

    UserMyPageResponseDto getUserMypage(Long userId, String dataType);

    UserResponseDto.Userdto updateUserProfile(UserUpdateRequestDto requestDto);

    String createNickname(Role role);

    public String saveNickname(String nickname);

    void deleteUser(String accessToken);

    String logout(String aToken, String refreshToken);
}
