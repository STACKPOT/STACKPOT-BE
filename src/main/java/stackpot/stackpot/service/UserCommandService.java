package stackpot.stackpot.service;

import stackpot.stackpot.domain.User;
import stackpot.stackpot.web.dto.UserRequestDto;

public interface UserCommandService {

    public User joinUser(UserRequestDto.JoinDto request);
    public User saveNewUser(String email);
}
