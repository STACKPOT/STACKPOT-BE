package stackpot.stackpot.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.UserHandler;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.user.dto.request.MyDescriptionRequestDto;
import stackpot.stackpot.user.dto.response.MyDescriptionResponseDto;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.entity.enums.Role;
import stackpot.stackpot.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;
    private final AuthService authService;

    @Override
    public String selectNameByUserId(Long userId) {
        return userRepository.findNameByUserId(userId).orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
    }

    public MyDescriptionResponseDto getMyDescription() {
        User user = authService.getCurrentUser();
        String Description = user.getUserDescription() != null ? user.getUserDescription() : "";
        return new MyDescriptionResponseDto(Description);
    }
    public MyDescriptionResponseDto getUserDescription(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        String description = user.getUserDescription() != null ? user.getUserDescription() : "";
        return new MyDescriptionResponseDto(description);
    }


}
