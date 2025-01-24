package stackpot.stackpot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import stackpot.stackpot.converter.UserConverter;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.repository.UserRepository.UserRepository;
import stackpot.stackpot.web.dto.ApplicantResponseDTO;
import stackpot.stackpot.web.dto.MypageUserResponseDto;
import stackpot.stackpot.web.dto.UserRequestDto;
import stackpot.stackpot.web.dto.UserResponseDto;

import java.util.Map;

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

    @Override
    public UserResponseDto getMypages() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // User 정보를 UserResponseDto로 변환
        return UserResponseDto.builder()
                .email(user.getEmail())
                .nickname(user.getNickname() + getVegetableNameByRole(user.getRole().name()))  // 닉네임 + 역할
                .role(user.getRole())
                .interest(user.getInterest())
                .userTemperature(user.getUserTemperature())
                .kakaoId(user.getKakaoId())
                .userIntroduction(user.getUserIntroduction())  // 한 줄 소개 추가
                .build();
    }

    @Override
    public UserResponseDto getUserMypage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        return UserResponseDto.builder()
                .email(user.getEmail())
                .nickname(user.getNickname() + getVegetableNameByRole(user.getRole().name())) // 닉네임 + 역할
                .role(user.getRole())
                .interest(user.getInterest())
                .userTemperature(user.getUserTemperature())
                .kakaoId(user.getKakaoId())
                .userIntroduction(user.getUserIntroduction())
                .build();
    }

    // 역할에 따른 채소명을 반환하는 메서드
    private String getVegetableNameByRole(String role) {
        Map<String, String> roleToVegetableMap = Map.of(
                "BACKEND", " 양파",
                "FRONTEND", " 버섯",
                "DESIGN", " 브로콜리",
                "PLANNING", " 당근"
        );
        return roleToVegetableMap.getOrDefault(role, "알 수 없음");
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
