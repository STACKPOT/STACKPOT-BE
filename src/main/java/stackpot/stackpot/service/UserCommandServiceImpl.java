package stackpot.stackpot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.MemberHandler;
import stackpot.stackpot.config.security.JwtTokenProvider;
import stackpot.stackpot.converter.UserConverter;
import stackpot.stackpot.converter.UserMypageConverter;
import stackpot.stackpot.domain.Feed;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.repository.BlacklistRepository;
import stackpot.stackpot.repository.FeedRepository.FeedRepository;
import stackpot.stackpot.repository.PotRepository.PotRepository;
import stackpot.stackpot.repository.RefreshTokenRepository;
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
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistRepository blacklistRepository;

    @Override
    @Transactional
    public UserSignUpResponseDto joinUser(UserRequestDto.JoinDto request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 1. 기존 사용자 조회 또는 새로운 사용자 생성
        User user = userRepository.findByEmail(email).orElseGet(() -> saveNewUser(email, request));

        // 2. 기존 사용자일 경우 업데이트
        if (user.getId() != null) {
            updateUserData(user, request);
            userRepository.save(user); // 기존 사용자 업데이트 후 저장
        }

        return UserConverter.toUserSignUpResponseDto(user);
    }

    @Override
    public User saveNewUser(String email, UserRequestDto.JoinDto request) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .kakaoId(request.getKakaoId())
                            .role(request.getRole())
                            .interest(request.getInterest())
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
        // 값이 존재하는 경우에만 업데이트
        if (request.getKakaoId() != null) user.setKakaoId(request.getKakaoId());
        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getInterest() != null) user.setInterest(request.getInterest());

        // 한 줄 소개 생성 (주석 해제 가능)
        /*if (request.getRole() != null && user.getNickname() != null) {
            user.setUserIntroduction(
                    request.getRole().name().trim() + "에 관심있는 " +
                            user.getNickname().trim() + getVegetableNameByRole(request.getRole().toString()).trim() + "입니다."
            );
        }*/
    }

    @Override
    public UserResponseDto.Userdto getMyUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // User 정보를 UserResponseDto로 변환
        return UserConverter.toDto(user);
    }

    @Override
    public UserResponseDto.Userdto getUsers(Long UserId) {
        User user = userRepository.findById(UserId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        return UserConverter.toDto(user);
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

        return UserConverter.toDto(user);
    }

    @Override
    @Transactional
    public String createNickname(Role role) {

        String nickname;

        while (true) {
            // 닉네임 생성
            String prompt = "닉네임을 만들려고 해, 닉네임은 양파, 버섯, 브로콜리, 당근 앞에 수식어를 붙여서 만들꺼야" +
                    "양파, 버섯, 브로콜리, 당근 앞에 올 수식어를 15글자 미만으로 알려줘" +
                    "아래는 수식어를 활용해 만든 닉네임 예시야" +
                    "에너제틱 양파, 불타는 버섯, 상쾌한 브로콜리, 질주하는 당근, 활활 타오르는 양파, 열정적인 버섯, " +
                    "싱글벙글 브로콜리, 끝까지 달리는 당근, 넘치는 힘의 양파, 신나는 버섯, 즐거운 브로콜리, 힘차게 뛰는 당근, " +
                    "활력 가득 양파, 기운 펄펄 버섯, 에너지가 넘치는 브로콜리, 웃음이 가득한 당근" +
                    "하나의 수식어만 알려주고 앞에 숫자와 뒤에 양파, 버섯 등등은 붙이지 말아줘";
            nickname = potSummarizationService.summarizeText(prompt, 15);

            // 중복 검사
            if (!userRepository.existsByNickname(nickname)) {
                break;
            }
        }

        return nickname+getVegetableNameByRole(getVegetableNameByRole(role.toString()));
    }

    @Override
    @Transactional
    public String saveNickname(String nickname) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        nickname = trimNickname(nickname);

        user.setNickname(nickname); // 유저 객체에 닉네임 저장
        user.setUserIntroduction(user.getRole() + "에 관심있는 " + nickname + getVegetableNameByRole(String.valueOf(user.getRole())) + "입니다.");
        userRepository.save(user); // DB에 저장

        return nickname + getVegetableNameByRole(user.getRole().toString());
    }

    private String trimNickname(String nickname) {
        // 앞뒤 공백 유지
        nickname = nickname.trim();

        // 채소 이름 리스트
        String[] vegetables = {"버섯", "양파", "브로콜리", "당근"};

        for (String vegetable : vegetables) {
            if (nickname.contains(" " + vegetable)) {
                // 공백과 함께 채소 이름을 제거
                return nickname.replace(" " + vegetable, "").trim();
            } else if (nickname.endsWith(vegetable)) {
                // 채소 이름이 맨 끝에 있는 경우 제거
                return nickname.replace(vegetable, "").trim();
            }
        }

        return nickname; // 기본적으로 원래 닉네임 반환
    }

    @Transactional
    public void deleteUser(String accessToken) {
        String token = accessToken.replace("Bearer ", "");
        String email = jwtTokenProvider.getEmailFromToken(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow( ()-> new IllegalArgumentException(("사용자를 찾을 수 없습니다.")));

        userRepository.delete(user);

        // Refresh Token 삭제 (로그아웃)
        refreshTokenRepository.deleteById(token);

        // Access Token 블랙리스트에 추가
        long expiration = jwtTokenProvider.getExpiration(token);
        blacklistRepository.addToBlacklist(token, expiration);

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
