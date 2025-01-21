package stackpot.stackpot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import stackpot.stackpot.converter.UserConverter;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.repository.UserRepository.UserRepository;
import stackpot.stackpot.web.dto.UserRequestDto;

@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService{

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User joinUser(UserRequestDto.JoinDto request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        updateUserData(user, request);

        return userRepository.save(user);
    }

    private void updateUserData(User user, UserRequestDto.JoinDto request) {
        // 카카오 id
        user.setKakaoId(request.getKakaoId());
        // 닉네임
        user.setNickname(request.getNickname());
        // 역할군
        user.setRole(request.getRole());
        // 관심사
        user.setInterest(request.getInterest());
    }
}
