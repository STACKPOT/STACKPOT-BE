package stackpot.stackpot.service;

import stackpot.stackpot.domain.User;
import stackpot.stackpot.web.dto.MypageUserResponseDto;
import stackpot.stackpot.web.dto.UserRequestDto;
import stackpot.stackpot.web.dto.UserResponseDto;

public interface UserCommandService {

    public User joinUser(UserRequestDto.JoinDto request);

    UserResponseDto getMypages();

    UserResponseDto getUserMypage(Long userId);
}
