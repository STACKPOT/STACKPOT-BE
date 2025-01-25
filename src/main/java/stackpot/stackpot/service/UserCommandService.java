package stackpot.stackpot.service;

import stackpot.stackpot.domain.User;
import stackpot.stackpot.web.dto.UserMypageResponseDto;
import stackpot.stackpot.web.dto.UserRequestDto;
import stackpot.stackpot.web.dto.UserResponseDto;
import stackpot.stackpot.web.dto.UserUpdateRequestDto;

public interface UserCommandService {
    User joinUser(UserRequestDto.JoinDto request);
    User saveNewUser(String email);

    UserResponseDto getMypages();

    UserMypageResponseDto getUserMypage(Long userId);

    UserResponseDto updateUserProfile(UserUpdateRequestDto requestDto);
}
