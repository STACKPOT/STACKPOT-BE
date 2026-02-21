package stackpot.stackpot.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.GeneralException;
import stackpot.stackpot.apiPayload.exception.handler.PotHandler;
import stackpot.stackpot.apiPayload.exception.handler.TokenHandler;
import stackpot.stackpot.apiPayload.exception.handler.UserHandler;
import stackpot.stackpot.chat.repository.ChatRoomInfoRepository;
import stackpot.stackpot.chat.repository.ChatRoomRepository;
import stackpot.stackpot.chat.service.chat.ChatCommandService;
import stackpot.stackpot.chat.service.chatroom.ChatRoomQueryService;
import stackpot.stackpot.chat.service.chatroominfo.ChatRoomInfoCommandService;
import stackpot.stackpot.chat.service.chatroominfo.ChatRoomInfoQueryService;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.config.security.JwtTokenProvider;
import stackpot.stackpot.pot.dto.UserMemberIdDto;
import stackpot.stackpot.pot.entity.mapping.PotSave;
import stackpot.stackpot.pot.repository.*;
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
import stackpot.stackpot.pot.service.pot.PotSummarizationService;
import stackpot.stackpot.badge.repository.PotMemberBadgeRepository;
import stackpot.stackpot.todo.repository.UserTodoRepository;
import stackpot.stackpot.feed.repository.FeedRepository;
import stackpot.stackpot.user.entity.enums.UserType;
import stackpot.stackpot.user.repository.BlacklistRepository;
import stackpot.stackpot.user.repository.RefreshTokenRepository;
import stackpot.stackpot.user.repository.TempUserRepository;
import stackpot.stackpot.user.repository.UserRepository;
import stackpot.stackpot.common.service.EmailService;

import java.util.ArrayList;
import java.util.Collections;
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
    private final ChatRoomInfoRepository chatRoominfoRepository;
    private final UserMypageConverter userMypageConverter;
    private final TempUserRepository tempUserRepository;
    private final PotSummarizationService potSummarizationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistRepository blacklistRepository;
    private final AuthService authService;
    private final PotSaveRepository potSaveRepository;
    private final EmailService emailService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatCommandService chatCommandService;


    @Override
    @Transactional
    public UserSignUpResponseDto joinUser(UserRequestDto.JoinDto request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        TempUser tempUserContext = (TempUser) authentication.getPrincipal();
        TempUser tempUser = tempUserRepository.findById(tempUserContext.getId())
                        .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        tempUser.setInterest(request.getInterest());
        tempUser.setRoles(request.getRoles());

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
                        .roles(user.getRoleNames())
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
                    .roles(Collections.emptyList())
                    .build();
        }
    }


//    private void updateUserData(User user, UserRequestDto.JoinDto request) {
//
//        // 값이 존재하는 경우에만 업데이트
//        if (request.getKakaoId() != null) user.setKakaoId(request.getKakaoId());
//        if (request.getRole() != null) user.setRole(request.getRole());
//        if (request.getInterest() != null) user.setInterests(request.getInterest());
//
//        // 한 줄 소개 생성 (주석 해제 가능)
//        /*if (request.getRole() != null && user.getNickname() != null) {
//            user.setUserIntroduction(
//                    request.getRole().name().trim() + "에 관심있는 " +
//                            user.getNickname().trim() + getVegetableNameByRole(request.getRole().toString()).trim() + "입니다."
//            );
//        }*/
//    }

    @Override
    public UserResponseDto.UserInfoDto getMyUsers() {
        User user = authService.getCurrentUser();
        if (user.getRoles().contains(Role.UNKNOWN)){
            log.error("탈퇴한 유저에 대한 요청입니다. {}",user.getUserId());
            throw new UserHandler(ErrorStatus.USER_NOT_FOUND);
        }
        return UserConverter.toUserInfo(user);
    }

    @Override
    public UserResponseDto.UserInfoDto getUsers(Long UserId) {
        User user = userRepository.findById(UserId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        //탈퇴한 사용자
        if (user.getRoles().contains(Role.UNKNOWN)){
            log.error("탈퇴한 유저에 대한 요청입니다. {}",user.getUserId());
            throw new UserHandler(ErrorStatus.USER_ALREADY_WITHDRAWN);
        }
        return UserConverter.toUserInfo(user);
    }


    public UserMyPageResponseDto getMypages() {
        User user = authService.getCurrentUser();

        // 탈퇴한 사용자
        if (user.getRoles().contains(Role.UNKNOWN)) {
            log.error("탈퇴한 유저에 대한 요청입니다. {}", user.getUserId());
            throw new UserHandler(ErrorStatus.USER_ALREADY_WITHDRAWN);
        }

        return getMypageByUser(user.getId());
    }

    public UserMyPageResponseDto getUserMypage(Long userId) {
        return getMypageByUser(userId);
    }

    private UserMyPageResponseDto getMypageByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        if (user.getRoles().contains(Role.UNKNOWN)) {
            throw new UserHandler(ErrorStatus.USER_ALREADY_WITHDRAWN);
        }

        // Feed만 조회
        List<Feed> feeds = feedRepository.findByUser_Id(userId);


        return userMypageConverter.toDto(user, feeds);
    }


    @Transactional
    public UserResponseDto.Userdto updateUserProfile(UserUpdateRequestDto requestDto) {
        // 현재 로그인한 사용자 정보 가져오기

        User user = authService.getCurrentUser();

        // 업데이트할 필드 적용
        if (requestDto.getRoles() != null && !requestDto.getRoles().isEmpty()) {
            user.setRoles(requestDto.getRoles()); //
        }

        if (requestDto.getInterest() != null && !requestDto.getInterest().isEmpty()) {
            user.setInterests(requestDto.getInterest());
        }
        if (requestDto.getUserIntroduction() != null && !requestDto.getUserIntroduction().isEmpty()) {
            user.setUserIntroduction(requestDto.getUserIntroduction());
        }
        // 저장 후 DTO로 변환하여 반환
        userRepository.save(user);

        return UserConverter.toDto(user);
    }

    @Override
    @Transactional
    public NicknameResponseDto createNickname() {
        String nickname;

        while (true) {
            // 닉네임 생성
            String prompt = getPromptForNewbie();
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
        return new NicknameResponseDto(nickname+" 새싹");
    }

    @Override
    @Transactional
    public TokenServiceResponse saveNickname(String nickname) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long tempUserId = ((TempUser) auth.getPrincipal()).getId();

        TempUser tempUser = tempUserRepository.findWithRolesById(tempUserId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        nickname = trimNickname(nickname);

        String intro = tempUser.getRoles().isEmpty()
                ? nickname + " 새싹입니다."
                : tempUser.getRoles().stream()
                .map(Role::getKoreanName)
                .collect(Collectors.joining(", ")) + "에 관심있는 " + nickname + " 새싹입니다.";

        List<String> interests = tempUser.getInterest() != null
                ? new ArrayList<>(tempUser.getInterest())
                : new ArrayList<>();

        List<Role> roles = tempUser.getRoles() != null
                ? new ArrayList<>(tempUser.getRoles())
                : new ArrayList<>();

        User user = User.builder()
                .email(tempUser.getEmail())
                .nickname(nickname)
                .userType(UserType.USER)
                .interests(interests)
                .userIntroduction(intro)
                .roles(roles)
                .userTemperature(33)
                .kakaoId(tempUser.getKakaoId())
                .provider(tempUser.getProvider())
                .providerId(tempUser.getProviderId())
                .build();

        userRepository.save(user);
        tempUserRepository.deleteById(tempUser.getId());

        return jwtTokenProvider.createToken(user.getUserId(), user.getProvider(), user.getUserType(), user.getEmail());
    }


    private String trimNickname(String nickname) {
        // 앞뒤 공백 유지
        log.info("닉네임 생성 전 닉네임: {}", nickname);
        nickname = nickname.trim();

        // 채소 이름 리스트
        String[] vegetables = {"버섯", "양파", "브로콜리", "당근", "새싹"};

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

            // Feed 관련 데이터 삭제
//            deleteFeedRelatedData(user.getId());

            // Todo 데이터 삭제
            userTodoRepository.deleteByUserId(user.getId());

            // Task 및 Taskboard 관련 데이터 삭제
            deleteTaskRelatedData(user.getId());

            // 사용자가 저장한 Pot 삭제
            potSaveRepository.deleteByUser(user);

            // Pot 관련 데이터 삭제
            boolean isCreator = potRepository.existsByUserId(user.getId());
            if (isCreator) {
                handleCreatorPotDeletion(user);
            } else {
                handleNormalUserPotDeletion(user);
            }
            // 토큰 블랙리스트 처리
            blacklistRepository.addToBlacklist(token, jwtTokenProvider.getExpiration(token));
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
//        feedRepository.deleteByUserId(userId);
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

    @Transactional
    public void handleCreatorPotDeletion(User user) {
        Long userId = user.getId();


        List<Long> completedPotIds = potRepository.findIdsByUserIdAndStatus(userId, "COMPLETED");
        List<Long> recruitingPotIds = potRepository.findIdsByUserIdAndStatus(userId, "RECRUITING");
        // 완료/모집중을 제외한 나머지(진행중 등)
        List<Long> otherPotIds = potRepository.findIdsByUserIdAndStatusNotIn(
                userId, List.of("COMPLETED", "RECRUITING")
        );

        // 2) 완료된 Pot → 현재 유저의 PotMember만 일괄 소프트 딜리트 (배치 UPDATE)
//        if (!completedPotIds.isEmpty()) {
//            potMemberRepository.softDeleteByPotIdsAndUserId(completedPotIds, userId);
//        }
        
        // 3) 모집중 Pot → 연관 정리 후 배치 삭제
        if (!recruitingPotIds.isEmpty()) {
            potRecruitmentDetailsRepository.deleteByPotIds(recruitingPotIds);
            // PotApplication 삭제
            potApplicationRepository.deleteByPotIds(recruitingPotIds);
            potSaveRepository.deleteByPotIds(recruitingPotIds);

            potRepository.deleteByUserIdAndPotIds(userId, recruitingPotIds);
        }


        // 4) 진행 중(기타 상태) → 권한 위임 요구 에러
        if (!otherPotIds.isEmpty()) {
            throw new PotHandler(ErrorStatus.POT_OWNERSHIP_TRANSFER_REQUIRED);
        }

        // 5) 유저 소프트 딜리트
        user.deleteUser();
        userRepository.save(user);
    }

    @Transactional
    public void deletePotAndRelatedData(List<Long> potIds) {
        List<Pot> pots = potRepository.findAllById(potIds);
        for (Pot pot : pots) {
            deletePotAndRelatedData(pot);
        }
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
            potSaveRepository.deleteByPotIds(List.of(pot.getPotId()));
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

    @Transactional
    public void handleNormalUserPotDeletion(User user) {
        Long userId = user.getId();
        // 1. 진행 중인 팟 IDs 조회
        List<Long> ongoingPotIds = potRepository.findIdsByUserIdAndStatus(userId, "ONGOING");

        // 2. 진행 중인 팟에서 PotMember 삭제
        if (!ongoingPotIds.isEmpty()) {
            potMemberRepository.deleteByUserIdAndPotIdIn(userId, ongoingPotIds);

            // 3. 진행 중인 팟에 해당하는 채팅방 ID들 조회
            List<Long> chatRoomIds = chatRoomRepository.findIdsByPotIdIn(ongoingPotIds);

            // 4. 각 채팅방에 대해 해당 유저의 채팅 메시지 삭제
            for (Long chatRoomId : chatRoomIds) {
                chatCommandService.deleteChatMessage(userId, chatRoomId);
            }
        }
        // 4. PotApplication 삭제
        potApplicationRepository.deleteByUserId(userId);
        user.deleteUser();
        userRepository.save(user);
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

    private String getPromptForNewbie() {
        return "너는 '프로젝트를 시작하고 싶은 사람'을 위한 닉네임 수식어 생성기야. " +
                "특히 '새싹'처럼 아직 작지만 앞으로 성장할 가능성이 있는 사람의 감성을 담아야 해. " +
                "배움, 도전, 시작, 성장, 열정, 호기심 같은 분위기를 담아서 만들어줘. " +
                "닉네임은 반드시 **수식어만 포함**해야 하고, **뒤에는 어떤 단어도 붙이면 안 돼.** " +
                "**특히 '버섯', '브로콜리', '양파', '당근', '새싹' 같은 단어는 쓰지 마.** " +
                "닉네임은 15자 이내여야 하고, 특수문자와 숫자는 포함하지 마. " +
                "예시:\n" +
                "- 도전을 즐기는\n" +
                "- 배우고 싶은\n" +
                "- 아이디어가 자라는\n" +
                "- 가능성을 품은\n" +
                "- 첫걸음을 내딛는\n" +
                "- 매일 조금씩 성장하는\n" +
                "- 호기심으로 가득한\n" +
                "- 열정이 피어나는\n" +
                "- 아직 미완성이지만 빛나는\n" +
                "이제 '프로젝트를 시작하고 싶은 새싹'의 감성을 담은 닉네임 수식어 하나를 만들어줘.";
    }

    @Transactional
    public MyDescriptionResponseDto upsertDescription(MyDescriptionRequestDto dto) {
        User user = authService.getCurrentUser();

        user.updateUserDescription(dto.getUserDescription());

        userRepository.save(user);

        return new MyDescriptionResponseDto(user.getUserDescription());
    }


    @Transactional
    public void deleteDescription() {
        User user = authService.getCurrentUser();
        user.updateUserDescription(null);
        userRepository.save(user);
    }
}
