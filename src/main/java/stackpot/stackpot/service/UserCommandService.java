package stackpot.stackpot.service;

import stackpot.stackpot.domain.User;
import stackpot.stackpot.web.dto.*;

public interface UserCommandService {
    User joinUser(UserRequestDto.JoinDto request);
    User saveNewUser(String email);

    UserResponseDto.loginDto isnewUser(String email);

    UserResponseDto.Userdto getMyUsers();
    UserResponseDto.Userdto getUsers(Long UserId);

    UserMypageResponseDto getMypages(String dataType);

    UserMypageResponseDto getUserMypage(Long userId, String dataType);

    UserResponseDto.Userdto updateUserProfile(UserUpdateRequestDto requestDto);

    String createNickname();
}
