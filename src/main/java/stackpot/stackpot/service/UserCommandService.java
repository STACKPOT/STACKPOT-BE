package stackpot.stackpot.service;

import stackpot.stackpot.domain.User;
import stackpot.stackpot.web.dto.UserMypageResponseDto;
import stackpot.stackpot.web.dto.UserRequestDto;
import stackpot.stackpot.web.dto.UserResponseDto;

public interface UserCommandService {

    public User joinUser(UserRequestDto.JoinDto request);
    public User saveNewUser(String email);

    UserResponseDto getMypages();

    UserMypageResponseDto getUserMypage(Long userId);
}
