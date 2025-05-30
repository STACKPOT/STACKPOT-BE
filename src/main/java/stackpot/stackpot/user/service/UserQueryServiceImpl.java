package stackpot.stackpot.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.UserHandler;
import stackpot.stackpot.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    @Override
    public String selectNameByUserId(Long userId) {
        return userRepository.findNameByUserId(userId).orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
    }
}
