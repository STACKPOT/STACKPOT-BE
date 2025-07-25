package stackpot.stackpot.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.GeneralException;
import stackpot.stackpot.apiPayload.exception.handler.TokenHandler;
import stackpot.stackpot.apiPayload.exception.handler.UserHandler;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.config.security.JwtTokenProvider;
import stackpot.stackpot.task.repository.TaskRepository;
import stackpot.stackpot.task.repository.TaskboardRepository;
import stackpot.stackpot.user.converter.UserConverter;
import stackpot.stackpot.user.converter.UserMypageConverter;
import stackpot.stackpot.feed.entity.Feed;
import stackpot.stackpot.feed.repository.FeedLikeRepository;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.task.entity.Taskboard;
import stackpot.stackpot.user.dto.request.MyDescriptionRequestDto;
import stackpot.stackpot.user.dto.request.UserRequestDto;
import stackpot.stackpot.user.dto.request.UserUpdateRequestDto;
import stackpot.stackpot.user.dto.response.*;
import stackpot.stackpot.user.entity.TempUser;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.entity.enums.Provider;
import stackpot.stackpot.user.entity.enums.Role;
import stackpot.stackpot.pot.entity.mapping.PotMember;
import stackpot.stackpot.pot.repository.PotMemberRepository;
import stackpot.stackpot.pot.service.pot.PotSummarizationService;
import stackpot.stackpot.badge.repository.PotMemberBadgeRepository;
import stackpot.stackpot.todo.repository.UserTodoRepository;
import stackpot.stackpot.feed.repository.FeedRepository;
import stackpot.stackpot.pot.repository.PotApplicationRepository;
import stackpot.stackpot.pot.repository.PotRecruitmentDetailsRepository;
import stackpot.stackpot.pot.repository.PotRepository;
import stackpot.stackpot.user.entity.enums.UserType;
import stackpot.stackpot.user.repository.BlacklistRepository;
import stackpot.stackpot.user.repository.RefreshTokenRepository;
import stackpot.stackpot.user.repository.TempUserRepository;
import stackpot.stackpot.user.repository.UserRepository;
import stackpot.stackpot.common.service.EmailService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final PotRepository potRepository;
    private final FeedRepository feedRepository;
    private final PotMemberRepository potMemberRepository;
    private final PotApplicationRepository potApplicationRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final UserTodoRepository userTodoRepository;
    private final TaskRepository taskRepository;
    private final TaskboardRepository taskboardRepository;
    private final PotRecruitmentDetailsRepository potRecruitmentDetailsRepository;
    private final PotMemberBadgeRepository potMemberBadgeRepository;

    private final UserMypageConverter userMypageConverter;
    private final TempUserRepository tempUserRepository;
    private final PotSummarizationService potSummarizationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistRepository blacklistRepository;
    private final AuthService authService;

    private final EmailService emailService;

    @Override
    @Transactional
    public UserSignUpResponseDto joinUser(UserRequestDto.JoinDto request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        TempUser tempUserContext = (TempUser) authentication.getPrincipal();
        TempUser tempUser = tempUserRepository.findById(tempUserContext.getId())
                        .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        tempUser.setInterest(request.getInterest());
        tempUser.setRole(request.getRole());
        tempUser.setKakaoId(request.getKakaoId());

        tempUserRepository.save(tempUser);

        return UserConverter.toUserSignUpResponseDto(tempUser);
    }

    @Override
    public UserResponseDto.loginDto isnewUser(Provider provider, String providerId, String email) {
        //provider+providId로 조회
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(provider, providerId);

        if (existingUser.isPresent()) {
                // 기존 유저가 있으면 isNewUser = false
                User user = existingUser.get();
                TokenServiceResponse token = jwtTokenProvider.createToken(user.getUserId(), user.getProvider(), user.getUserType(), user.getEmail());

                return UserResponseDto.loginDto.builder()
                        .tokenServiceResponse(token)
                        .isNewUser(false)
                        .role(user.getRole())
                        .build();
        }
        else {
            TempUser newUser = TempUser.builder()
                    .provider(provider)
                    .providerId(providerId)
                    .email(email)
                    .build();

            tempUserRepository.save(newUser);
            TokenServiceResponse token = jwtTokenProvider.createToken(newUser.getId(), newUser.getProvider(), UserType.TEMP, newUser.getEmail());

            return UserResponseDto.loginDto.builder()
                    .tokenServiceResponse(token)
                    .isNewUser(true)  // 신규 유저임을 표시
                    .role(null)
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
        User user = authService.getCurrentUser();
        if(user.getRole() == Role.UNKNOWN){
            log.error("탈퇴한 유저에 대한 요청입니다. {}",user.getUserId());
            throw new UserHandler(ErrorStatus.USER_NOT_FOUND);
        }
        // User 정보를 UserResponseDto로 변환
        return UserConverter.toDto(user);
    }

    @Override
    public UserResponseDto.Userdto getUsers(Long UserId) {
        User user = userRepository.findById(UserId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        //탈퇴한 사용자
        if(user.getRole() == Role.UNKNOWN){
            log.error("탈퇴한 유저에 대한 요청입니다. {}",user.getUserId());
            throw new UserHandler(ErrorStatus.USER_ALREADY_WITHDRAWN);
        }
        return UserConverter.toDto(user);
    }


    public UserMyPageResponseDto getMypages(String dataType) {
        User user = authService.getCurrentUser();

        //탈퇴한 사용자
        if(user.getRole() == Role.UNKNOWN){
            log.error("탈퇴한 유저에 대한 요청입니다. {}",user.getUserId());
            throw new UserHandler(ErrorStatus.USER_ALREADY_WITHDRAWN);
        }

        return getMypageByUser(user.getId(), dataType);
    }

    public UserMyPageResponseDto getUserMypage(Long userId, String dataType) {
        return getMypageByUser(userId, dataType);
    }

    private UserMyPageResponseDto getMypageByUser(Long userId, String dataType) {
        List<Pot> completedPots = List.of();
        List<Feed> feeds = List.of();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        if(user.getRole() == Role.UNKNOWN){
            throw new UserHandler(ErrorStatus.USER_ALREADY_WITHDRAWN);
        }

        if (dataType == null || dataType.isBlank()) {
            completedPots = potRepository.findByUserIdAndPotStatus(userId, "COMPLETED");
            feeds = feedRepository.findByUser_Id(userId);
        } else if ("pot".equalsIgnoreCase(dataType)) {
            completedPots = potRepository.findByUserIdAndPotStatus(userId, "COMPLETED");
        } else if ("feed".equalsIgnoreCase(dataType)) {
            feeds = feedRepository.findByUser_Id(userId);
        } else {
            log.error("pot, feed의 요청이 잘 못 되었습니다.");
            throw new GeneralException(ErrorStatus._BAD_REQUEST);
        }
        return userMypageConverter.toDto(user, completedPots, feeds);
    }


    @Transactional
    public UserResponseDto.Userdto updateUserProfile(UserUpdateRequestDto requestDto) {
        // 현재 로그인한 사용자 정보 가져오기

        User user = authService.getCurrentUser();

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
    public NicknameResponseDto createNickname(Role role) {
        String nickname;

        while (true) {
            // 닉네임 생성
            String prompt = getPromptByRole(role);
            nickname = potSummarizationService.summarizeText(prompt, 15);

            // 중복 검사
            if (!userRepository.existsByNickname(nickname)) {
                log.info("닉네임이 생성되었습니다.{}",nickname);
                break;
            }
            else {
                log.debug("사용중인 닉네임 입니다.{}", nickname);
            }
        }
        return new NicknameResponseDto(nickname + " " + Role.toVegetable(role.toString()));
    }

    @Override
    @Transactional
    public TokenServiceResponse saveNickname(String nickname) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        TempUser tempUser = (TempUser) authentication.getPrincipal();
        log.info("tempUser {} ",tempUser.getId());

        nickname = trimNickname(nickname);

        User user = User.builder()
                .email(tempUser.getEmail())
                .nickname(nickname)
                .userType(UserType.USER)
                .interest(tempUser.getInterest())
                .userIntroduction(tempUser.getRole() + "에 관심있는 " + nickname + " " + Role.toVegetable(String.valueOf(tempUser.getRole())) + "입니다.")
                .userTemperature(33)
                .kakaoId(tempUser.getKakaoId())
                .provider(tempUser.getProvider())
                .providerId(tempUser.getProviderId())
                .role(tempUser.getRole())
                .build();

        userRepository.save(user); // DB에 저장
        tempUserRepository.delete(tempUser);

        TokenServiceResponse tokenServiceResponse = jwtTokenProvider.createToken(user.getUserId(), user.getProvider(), user.getUserType(), user.getEmail());

        return tokenServiceResponse;
    }

    private String trimNickname(String nickname) {
        // 앞뒤 공백 유지
        log.info("닉네임 생성 전 닉네임: {}", nickname);
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
        log.info("닉네임 생성 후 닉네임: {}", nickname);
        return nickname; // 기본적으로 원래 닉네임 반환
    }

    //todo 재개발 필요
    @Transactional
    public String deleteUser(String accessToken) {
        // 1. 토큰 검증 및 사용자 조회
        String token = accessToken.replace("Bearer ", "");
        User user = authService.getCurrentUser();

        log.info("회원 탈퇴 시작 id:{}", user.getUserId());

        try {
            // 토큰 블랙리스트 처리
            blacklistRepository.addToBlacklist(token, jwtTokenProvider.getExpiration(token));

            // Feed 관련 데이터 삭제
            deleteFeedRelatedData(user.getId());

            // Todo 데이터 삭제
            userTodoRepository.deleteByUserId(user.getId());

            // Task 및 Taskboard 관련 데이터 삭제
            deleteTaskRelatedData(user.getId());

            // Pot 관련 데이터 삭제
            boolean isCreator = potRepository.existsByUserId(user.getId());
            if (isCreator) {
                handleCreatorPotDeletion(user);
            } else {
                handleNormalUserPotDeletion(user);
            }
            return "회원 탈퇴가 완료되었습니다.";

        } catch (Exception e) {
            log.error("회원 탈퇴 중 오류 발생: {}", e.getMessage(), e);
            throw new UserHandler(ErrorStatus.USER_WITHDRAWAL_FAILED);
        }
    }

    private void deleteFeedRelatedData(Long userId) {
        // Feed 좋아요 삭제
        feedLikeRepository.deleteByUserId(userId);

        // Feed 삭제
        feedRepository.deleteByUserId(userId);
    }

    private void deleteTaskRelatedData(Long userId) {
        // PotMember 관련 데이터 조회
        List<PotMember> potMembers = potMemberRepository.findByUserId(userId);
        List<Long> potMemberIds = potMembers.stream()
                .map(PotMember::getPotMemberId)
                .collect(Collectors.toList());

        // Badge 데이터 삭제
        if (!potMemberIds.isEmpty()) {
            potMemberBadgeRepository.deleteByPotMemberIds(potMemberIds);
        }

        // Taskboard 및 Task 삭제
        // 탈퇴하는 유자가 taskboard 생성자
        List<Taskboard> taskboards = taskboardRepository.findByUserId(userId);
        if (!taskboards.isEmpty()) {
            List<Long> taskboardIds = taskboards.stream()
                    .map(Taskboard::getTaskboardId)
                    .collect(Collectors.toList());

            taskRepository.deleteByTaskboardIds(taskboardIds);
            taskboardRepository.deleteAll(taskboards);
        }

        // Task 삭제 (PotMember 관련)
        if (!potMemberIds.isEmpty()) {
            taskRepository.deleteByPotMemberIds(potMemberIds);
        }
    }

    private void handleCreatorPotDeletion(User user) {
        List<Pot> userPots = potRepository.findByUserId(user.getId());

        for (Pot pot : userPots) {
            if (pot.getPotStatus().equals("COMPLETED")) {
                // 완료된 Pot의 경우 PotMember만 소프트 딜리트
                PotMember potMember = potMemberRepository.findByPotIdAndUserId(pot.getPotId(), user.getId());
                potMember.deletePotMember();
                potMemberRepository.save(potMember);
            } else {
                // 진행 중인 Pot 처리
                deletePotAndRelatedData(pot);
            }
        }

        user.deleteUser();  // 소프트 딜리트
        userRepository.save(user);
    }

    @Transactional
    public void deletePotAndRelatedData(Pot pot) {


        // PotMember 조회 및 ID 추출
        List<PotMember> potMembers = potMemberRepository.findByPotId(pot.getPotId());
        List<Long> potMemberIds = potMembers.stream()
                .map(PotMember::getPotMemberId)
                .collect(Collectors.toList());



        if (pot.getPotStatus().equals("ONGOING")) {
            sendDeletionNotifications(potMembers, pot);
        }

        try {
            // Todo 삭제
            userTodoRepository.deleteByPotId(pot.getPotId());

            // Task 관련 데이터 삭제
            if (!potMemberIds.isEmpty()) {
                taskRepository.deleteByPotMemberIds(potMemberIds);
                potMemberBadgeRepository.deleteByPotMemberIds(potMemberIds);
            }

            // Taskboard 삭제
            taskboardRepository.deleteByPotId(pot.getPotId());

            // 각 PotMember의 application 참조 제거
            potMemberRepository.clearApplicationReferences(pot.getPotId());


            // PotMember 삭제
            potMemberRepository.deleteByPotId(pot.getPotId());


            // PotApplication 삭제
            potApplicationRepository.deleteByPotId(pot.getPotId());

            potRecruitmentDetailsRepository.deleteByPot_PotId(pot.getPotId());

            // Pot 삭제
            potRepository.delete(pot);

        } catch (Exception e) {
            throw new UserHandler(ErrorStatus.USER_WITHDRAWAL_FAILED);
        }
    }

    private void sendDeletionNotifications(List<PotMember> potMembers, Pot pot) {
        potMembers.forEach(potMember -> {
            try {
                emailService.sendPotDeleteNotification(
                        potMember.getUser().getEmail(),
                        pot.getPotName(),
                        potMember.getUser().getNickname() + " " + Role.toVegetable(potMember.getRoleName().name())
                );
            } catch (Exception e) {
                // 이메일 발송 실패는 전체 프로세스를 중단하지 않음
                log.error("이메일 발송 실패: {}", e.getMessage());
            }
        });
    }

    private void handleNormalUserPotDeletion(User user) {
        potMemberRepository.deleteByUserId(user.getId());
        potApplicationRepository.deleteByUserId(user.getId());
        userRepository.delete(user);
    }

    @Override
    public String logout(String aToken, String refreshToken) {
        String accessToken = aToken.replace("Bearer ", "");
        User user = authService.getCurrentUser();

        try {
            // refreshToken 삭제 (존재하지 않아도 예외를 던지지 않도록 함)
            refreshTokenRepository.deleteToken(refreshToken);
            log.info("Refresh Token 삭제 성공 refreshToken :{}",refreshToken);
        } catch (Exception e) {
            log.info("로그아웃 실패 실패 된 유저 id:{}",user.getUserId());
            log.info("refresh 삭제 중 오류 발생 {}",e.getMessage());
            throw new TokenHandler(ErrorStatus.REDIS_KEY_NOT_FOUND);
        }

        long expiration = jwtTokenProvider.getExpiration(accessToken);

        try {
            // 블랙리스트에 추가
            blacklistRepository.addToBlacklist(accessToken, expiration);
        } catch (Exception e) {
            log.info("로그아웃 실패 실패 된 유저 id{}",user.getUserId());
            log.info("토큰 블랙리스트 등록 중 오류 발생 {}",e.getMessage());
            throw new TokenHandler(ErrorStatus.REDIS_BLACKLIST_SAVE_FAILED);
        }
        return "로그아웃이 성공적으로 완료되었습니다.";
    }

    private String getPromptByRole(Role role) {
        switch (role) {
            case BACKEND:
                return "너는 백엔드 개발자 전용 닉네임 생성기야. " +
                        "백엔드 개발자는 서버를 다루고, API를 만들고, 때때로 버그와 싸우는 존재야. " +
                        "이들의 일상적인 특성과 분위기를 반영한 닉네임을 만들어줘. " +
                        "닉네임은 반드시 **수식어만 포함**해야 하고, **뒤에는 어떤 단어도 붙이면 안 돼.** " +
                        "**특히 '버섯', '브로콜리', '양파', '당근'을 수식어 끝에 붙이지 마.** " +
                        "닉네임은 15자 이내여야 하고, 특수문자와 숫자는 포함하지 마. " +
                        "예시:\n" +
                        "- 묵묵한 해결사\n" +
                        "- 밤을 지배하는\n" +
                        "- 디버깅 철학자\n" +
                        "- 재부팅이 답이야\n" +
                        "- 커피로 살아가는\n" +
                        "- 감자칩과 코드\n" +
                        "- 무한 루프 탈출러\n" +
                        "- 배포의 순간 떨리는\n" +
                        "- 예상치 못한 오류의 친구\n" +
                        "- 로그에 의존하는 삶\n" +
                        "- 밤을 지새우는\n" +
                        "- 코드 속에 사는\n" +
                        "- 로딩 중인\n" +
                        "- 서버를 지키는\n" +
                        "- 로그를 수집하는\n" +
                        "- 언제나 배포 준비\n" +
                        "- 메모리 절약 장인\n" +
                        "- 예외를 쫓는\n" +
                        "이제 백엔드 개발자의 감성과 특징을 반영한 닉네임 수식어 하나를 만들어줘.";

            case FRONTEND:
                return "너는 프론트엔드 개발자 전용 닉네임 생성기야. " +
                        "프론트엔드 개발자들은 UI/UX, 디자인, 자바스크립트, 반응형 웹을 다루지. " +
                        "그래서 그들의 일상을 반영한 닉네임을 만들어야 해. " +
                        "닉네임은 반드시 수식어만 포함해야 하며, 뒤에는 어떤 단어도 붙이면 안 돼. " +
                        "**절대 '버섯', '브로콜리', '양파', '당근'을 수식어 끝에 붙이지 마.** " +
                        "닉네임은 15자 이내여야 하고, 특수문자와 숫자는 포함하지 마. " +
                        "예시:\n" +
                        "- 픽셀 하나까지 보는\n" +
                        "- 다크모드를 사랑하는\n" +
                        "- 마우스를 덜 쓰는\n" +
                        "- CSS와 함께 춤을\n" +
                        "- 반응형에 진심인\n" +
                        "- UI 디테일 집착러\n" +
                        "- 모니터를 끌 수 없는\n" +
                        "- 화면을 디자인하는\n" +
                        "- 색상 코드 장인\n" +
                        "- 픽셀 맞추기 장인\n" +
                        "- 감성적인 디버거\n" +
                        "- 버튼 하나에 2시간\n" +
                        "- 컬러팔레트 탐험가\n" +
                        "- 디자인과 현실 사이\n" +
                        "- 알림창을 사랑하는\n" +
                        "- CSS 트라우마 보유자\n" +
                        "- 예쁘면 다 용서됨\n" +
                        "이제 프론트엔드 개발자의 특징을 반영한 닉네임 수식어 하나를 만들어줘.";

            case DESIGN:
                return "너는 디자이너 전용 닉네임 생성기야. " +
                        "디자이너들은 색감, UI/UX, 레이아웃, 그래픽을 다루지. " +
                        "그래서 그들의 일상을 반영한 닉네임을 만들어야 해. " +
                        "닉네임은 반드시 수식어만 포함해야 하며, 뒤에는 어떤 단어도 붙이면 안 돼. " +
                        "**절대 '버섯', '브로콜리', '양파', '당근'을 수식어 끝에 붙이지 마.** " +
                        "닉네임은 15자 이내여야 하고, 특수문자와 숫자는 포함하지 마. " +
                        "예시:\n" +
                        "- 색감 조합 마법사\n" +
                        "- 폰트 정렬의 고수\n" +
                        "- 직관적 레이아웃 탐험가\n" +
                        "- 감각적인 픽셀러\n" +
                        "- 그리드 계산 장인\n" +
                        "- 비율 감각이 뛰어난\n" +
                        "- 포토샵 브러쉬 장인\n" +
                        "- 스타일 가이드의 전설\n" +
                        "- 시각적 균형의 추구자\n" +
                        "- UI를 사랑하는\n" +
                        "이제 디자이너의 특징을 반영한 닉네임 수식어 하나를 만들어줘.";

            case PLANNING:
                return "너는 기획자 전용 닉네임 생성기야. " +
                        "기획자들은 프로젝트 관리, 일정 조율, 요구사항 정의 등을 다루지. " +
                        "그래서 그들의 일상을 반영한 닉네임을 만들어야 해. " +
                        "닉네임은 반드시 수식어만 포함해야 하며, 뒤에는 어떤 단어도 붙이면 안 돼. " +
                        "**절대 '버섯', '브로콜리', '양파', '당근'을 수식어 끝에 붙이지 마.** " +
                        "닉네임은 15자 이내여야 하고, 특수문자와 숫자는 포함하지 마. " +
                        "예시:\n" +
                        "- 일정 조율의 달인\n" +
                        "- 요구사항 정리왕\n" +
                        "- 문서 마스터\n" +
                        "- 회의 천재\n" +
                        "- 일정 예측가\n" +
                        "- 스프린트 조종자\n" +
                        "- 기획 감각 천재\n" +
                        "- 협업 마스터\n" +
                        "- 마감 기한을 지키는\n" +
                        "- 모든 걸 계획하는\n" +
                        "- 문서를 정리하는\n" +
                        "- 회의를 조율하는\n" +
                        "- 협업을 최적화하는\n" +
                        "- 서비스의 길을 여는\n" +
                        "- 일정 조율의 달인\n" +
                        "- 프로젝트를 설계하는\n" +
                        "- 아이디어를 수집하는\n" +
                        "- 보고서를 사랑하는\n" +
                        "이제 기획자의 특징을 반영한 닉네임 수식어 하나를 만들어줘.";

            default:
                return "너는 개발자 전용 닉네임 생성기야. " +
                        "닉네임은 개발자들이 일상에서 자주 겪는 특징, 습관, 성격을 반영한 수식어로 만들어야 해. " +
                        "닉네임은 반드시 수식어만 포함해야 하며, 뒤에는 어떤 단어도 붙이면 안 돼. " +
                        "**절대 '버섯', '브로콜리', '양파', '당근'을 수식어 끝에 붙이지 마.** " +
                        "닉네임은 15자 이내여야 하고, 특수문자와 숫자는 포함하지 마. " +
                        "이제 개발자의 일상을 반영한 닉네임 수식어 하나를 만들어줘.";
        }
    }

    @Transactional
    public void upsertDescription(MyDescriptionRequestDto dto) {
        User user = authService.getCurrentUser();
        user.updateUserDescription(dto.getUserDescription());
        userRepository.save(user);
    }

    @Transactional
    public void deleteDescription() {
        User user = authService.getCurrentUser();
        user.updateUserDescription(null);
        userRepository.save(user);
    }
}
