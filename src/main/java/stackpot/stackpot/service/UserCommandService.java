package stackpot.stackpot.service;

import stackpot.stackpot.domain.User;
import stackpot.stackpot.web.dto.UserMypageResponseDto;
import stackpot.stackpot.web.dto.UserRequestDto;
import stackpot.stackpot.web.dto.UserResponseDto;
import stackpot.stackpot.web.dto.UserUpdateRequestDto;

public interface UserCommandService {
    User joinUser(UserRequestDto.JoinDto request);
    User saveNewUser(String email);

    UserResponseDto getMyUsers();
    UserResponseDto getUsers(Long UserId);

    UserMypageResponseDto getMypages(String dataType);

    UserMypageResponseDto getUserMypage(Long userId, String dataType);

    UserResponseDto updateUserProfile(UserUpdateRequestDto requestDto);

    String createNickname();
}
