package stackpot.stackpot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import stackpot.stackpot.converter.UserConverter;
import stackpot.stackpot.converter.UserMypageConverter;
import stackpot.stackpot.domain.Feed;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.repository.FeedRepository.FeedRepository;
import stackpot.stackpot.repository.PotRepository.PotRepository;
import stackpot.stackpot.repository.UserRepository.UserRepository;
import stackpot.stackpot.web.dto.UserMypageResponseDto;
import stackpot.stackpot.web.dto.UserRequestDto;
import stackpot.stackpot.web.dto.UserResponseDto;
import stackpot.stackpot.web.dto.UserUpdateRequestDto;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService{

    private final UserRepository userRepository;
    private final PotRepository potRepository;
    private final FeedRepository feedRepository;
    private final UserMypageConverter userMypageConverter;

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
    public User saveNewUser(String email) {

        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .userTemperature(33)
                            .build();

                    return userRepository.save(newUser);
                });
    }

    private void updateUserData(User user, UserRequestDto.JoinDto request) {
        // 카카오 id
        user.setKakaoId(request.getKakaoId());
        // 닉네임
        user.setNickname(request.getNickname());
        // 역할군
        user.setRole(Role.valueOf(String.valueOf(request.getRole())));
        // 관심사
        user.setInterest(request.getInterest());
        //한줄 소개
        user.setUserIntroduction(user.getRole()+"에 관심있는 "+user.getNickname()+"입니다.");
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

    /*@Transactional
    public UserMypageResponseDto getUserMypage(Long userId, String dataType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // COMPLETED 상태의 팟 조회
        List<Pot> completedPots = potRepository.findByUserIdAndPotStatus(userId, "COMPLETED");

        // 사용자의 피드 조회
        List<Feed> userFeeds = feedRepository.findByUser_Id(userId);

        // 컨버터를 사용하여 변환 (좋아요 개수 포함)
        return userMypageConverter.toDto(user, completedPots, userFeeds);
    }*/

    @Transactional
    public UserMypageResponseDto getUserMypage(Long userId, String dataType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        List<Pot> completedPots = List.of();
        List<Feed> feeds = List.of();

        if (dataType == null || dataType.isBlank()) {
            // 모든 데이터 반환 (pot + feed)
            completedPots = potRepository.findByUserIdAndPotStatus(userId, "COMPLETED");
            feeds = feedRepository.findByUser_Id(userId);
        } else if ("pot".equalsIgnoreCase(dataType)) {
            // 팟 정보만 반환
            completedPots = potRepository.findByUserIdAndPotStatus(userId, "COMPLETED");
        } else if ("feed".equalsIgnoreCase(dataType)) {
            // 피드 정보만 반환
            feeds = feedRepository.findByUser_Id(userId);
        } else {
            throw new IllegalArgumentException("Invalid data type. Use 'pot', 'feed', or leave empty for all data.");
        }

        return userMypageConverter.toDto(user, completedPots, feeds);
    }



    @Transactional
    public UserResponseDto updateUserProfile(UserUpdateRequestDto requestDto) {
        // 현재 로그인한 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // 업데이트할 필드 적용
        if (requestDto.getRole() != null) {
            user.setRole(requestDto.getRole());
        }
        if (requestDto.getInterest() != null && !requestDto.getInterest().isEmpty()) {
            user.setInterest(requestDto.getInterest());
        }
        if (requestDto.getUserIntroduction() != null && !requestDto.getUserIntroduction().isEmpty()) {
            user.setUserIntroduction(requestDto.getUserIntroduction());
        }

        // 저장 후 DTO로 변환하여 반환
        userRepository.save(user);

        return UserResponseDto.builder()
                .email(user.getEmail())
                .nickname(user.getNickname() + getVegetableNameByRole(user.getRole().name()))  // 닉네임 + 역할
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
}
