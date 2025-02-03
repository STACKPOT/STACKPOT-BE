package stackpot.stackpot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.MemberHandler;
import stackpot.stackpot.config.security.JwtTokenProvider;
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
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService{

    private final UserRepository userRepository;
    private final PotRepository potRepository;
    private final FeedRepository feedRepository;
    private final UserMypageConverter userMypageConverter;
    private final PotSummarizationService potSummarizationService;
    private final JwtTokenProvider jwtTokenProvider;

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

    @Override
    public UserResponseDto.loginDto isnewUser(String email) {
        // 이메일로 기존 유저 조회
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            // 기존 유저가 있으면 isNewUser = false
            User user = existingUser.get();
            TokenServiceResponse token = jwtTokenProvider.createToken(user);

            return UserResponseDto.loginDto.builder()
                    .tokenServiceResponse(token)
                    .isNewUser(false)
                    .build();
        } else {
            // 신규 유저 생성
            User newUser = User.builder()
                    .email(email)
                    .userTemperature(33)  // 기본값 설정
                    .build();

            userRepository.save(newUser);
            TokenServiceResponse token = jwtTokenProvider.createToken(newUser);

            return UserResponseDto.loginDto.builder()
                    .tokenServiceResponse(token)
                    .isNewUser(true)  // 신규 유저임을 표시
                    .build();
        }
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
        //user.setUserIntroduction(user.getRole()+"에 관심있는 "+user.getNickname()+getVegetableNameByRole(String.valueOf(user.getRole()))+"입니다.");
        user.setUserIntroduction(
                user.getRole().name().trim() + "에 관심있는 " +
                        user.getNickname().trim() +
                        getVegetableNameByRole(String.valueOf(user.getRole())).trim() + "입니다."
        );
    }

    @Override
    public UserResponseDto.Userdto getMyUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // User 정보를 UserResponseDto로 변환
        return UserResponseDto.Userdto.builder()
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
    public UserResponseDto.Userdto getUsers(Long UserId) {
        User user = userRepository.findById(UserId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // User 정보를 UserResponseDto로 변환
        return UserResponseDto.Userdto.builder()
                .email(user.getEmail())
                .nickname(user.getNickname() + getVegetableNameByRole(user.getRole().name()))  // 닉네임 + 역할
                .role(user.getRole())
                .interest(user.getInterest())
                .userTemperature(user.getUserTemperature())
                .kakaoId(user.getKakaoId())
                .userIntroduction(user.getUserIntroduction())  // 한 줄 소개 추가
                .build();
    }


    public UserMyPageResponseDto getMypages(String dataType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        return getMypageByUser(user.getId(), dataType);
    }

    public UserMyPageResponseDto getUserMypage(Long userId, String dataType) {
        return getMypageByUser(userId, dataType);
    }

    private UserMyPageResponseDto getMypageByUser(Long userId, String dataType) {
        List<Pot> completedPots = List.of();
        List<Feed> feeds = List.of();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        if (dataType == null || dataType.isBlank()) {
            completedPots = potRepository.findByUserIdAndPotStatus(userId, "COMPLETED");
            feeds = feedRepository.findByUser_Id(userId);
        } else if ("pot".equalsIgnoreCase(dataType)) {
            completedPots = potRepository.findByUserIdAndPotStatus(userId, "COMPLETED");
        } else if ("feed".equalsIgnoreCase(dataType)) {
            feeds = feedRepository.findByUser_Id(userId);
        } else {
            throw new IllegalArgumentException("Invalid data type. Use 'pot', 'feed', or leave empty for all data.");
        }

        return userMypageConverter.toDto(user, completedPots, feeds);
    }


    @Transactional
    public UserResponseDto.Userdto updateUserProfile(UserUpdateRequestDto requestDto) {
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
        if(requestDto.getKakaoId() != null && !requestDto.getKakaoId().isEmpty()) {
            user.setKakaoId(requestDto.getKakaoId());
        }

        // 저장 후 DTO로 변환하여 반환
        userRepository.save(user);

        return UserResponseDto.Userdto.builder()
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
        String prompt = "닉네임을 만들려고 해, 닉네임은 양파, 버섯, 브로콜리, 당근 앞에 수식어를 붙여서 만들꺼야" + "양파, 버섯, 브로콜리, 당근 앞에 올 수식어를 15글자 미만으로 알려줘" +
"아래는 수식어를 활용해 만든 닉네임 예시야" + "에너제틱 양파 불타는 버섯, 상쾌한 브로콜리, 질주하는 당근, 활활 타오르는 양파, 열정적인 버섯, 싱글벙글 브로콜리, 끝까지 달리는 당근, 넘치는 힘의 양파, 신나는 버섯, 즐거운 브로콜리, 힘차게 뛰는 당근, 활력 가득 양파, 기운 펄펄 버섯, 에너지가 넘치는 브로콜리, 웃음이 가득한 당근" +
                "하나의 수식어만 알려주고 앞에 숫자와 뒤에 양파, 버섯 등등은 붙이지 말아줘";
        String nickname = potSummarizationService.summarizeText(prompt, 15);
        return nickname;
    }

    // 역할에 따른 채소명을 반환하는 메서드
    private String getVegetableNameByRole(String role) {
        Map<String, String> roleToVegetableMap = Map.of(
                "BACKEND", "양파",
                "FRONTEND", "버섯",
                "DESIGN", "브로콜리",
                "PLANNING", "당근"
        );
        return roleToVegetableMap.getOrDefault(role, "알 수 없음");
    }
}
