package stackpot.stackpot.service;

import stackpot.stackpot.domain.User;
import stackpot.stackpot.web.dto.UserRequestDTO;

public interface UserCommandService {

    public User joinUser(UserRequestDTO.JoinDto request);
}
