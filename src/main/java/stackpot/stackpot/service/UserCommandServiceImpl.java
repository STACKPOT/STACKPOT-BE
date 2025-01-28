package stackpot.stackpot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.MemberHandler;
import stackpot.stackpot.converter.UserMypageConverter;
import stackpot.stackpot.domain.Feed;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.repository.FeedRepository.FeedRepository;
import stackpot.stackpot.repository.PotRepository.PotRepository;
import stackpot.stackpot.repository.UserRepository.UserRepository;
import stackpot.stackpot.web.dto.*;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService{

    private final UserRepository userRepository;
    private final PotRepository potRepository;
    private final FeedRepository feedRepository;
    private final UserMypageConverter userMypageConverter;
    private final PotSummarizationService potSummarizationService;

    @Override
    @Transactional
    public User joinUser(UserRequestDto.JoinDto request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

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
        user.setRole(request.getRole());
        // 관심사
        user.setInterest(request.getInterest());
        //한줄 소개
        user.setUserIntroduction(user.getRole()+"에 관심있는 "+user.getNickname()+"입니다.");
    }

    @Override
    public UserResponseDto getMyUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

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
    public UserResponseDto getUsers(Long UserId) {
        User user = userRepository.findById(UserId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

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
    public UserMypageResponseDto getMypages(String dataType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        List<Pot> completedPots = List.of();
        List<Feed> feeds = List.of();

        if (dataType == null || dataType.isBlank()) {
            // 모든 데이터 반환 (pot + feed)
            completedPots = potRepository.findByUserIdAndPotStatus(user.getId(), "COMPLETED");
            feeds = feedRepository.findByUser_Id(user.getId());
        } else if ("pot".equalsIgnoreCase(dataType)) {
            // 팟 정보만 반환
            completedPots = potRepository.findByUserIdAndPotStatus(user.getId(), "COMPLETED");
        } else if ("feed".equalsIgnoreCase(dataType)) {
            // 피드 정보만 반환
            feeds = feedRepository.findByUser_Id(user.getId());
        } else {
            throw new IllegalArgumentException("Invalid data type. Use 'pot', 'feed', or leave empty for all data.");
        }

        return userMypageConverter.toDto(user, completedPots, feeds);
    }

    @Transactional
    public UserMypageResponseDto getUserMypage(Long userId, String dataType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

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
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

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

    @Override
    public String createNickname() {
        String prompt = "“재미있고 긍정적인 형용사와 명사를 결합한 문구를 만들어 주세요. 형식은 ‘형용사 명사’입니다"
                + "예를 들어, ‘잘 자라는 양파’, ‘힘이 넘치는 버섯’ 같은 느낌으로 작성해 주세요.”";

        String nickname = potSummarizationService.summarizeText(prompt, 15);

        return nickname;
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
