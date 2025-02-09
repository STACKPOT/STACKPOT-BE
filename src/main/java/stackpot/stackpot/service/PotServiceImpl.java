// Service
package stackpot.stackpot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.ApplicationHandler;
import stackpot.stackpot.apiPayload.exception.handler.MemberHandler;
import stackpot.stackpot.apiPayload.exception.handler.PotHandler;
import stackpot.stackpot.config.security.JwtTokenProvider;
import stackpot.stackpot.converter.MyPotConverter;
import stackpot.stackpot.converter.PotConverter;
import stackpot.stackpot.converter.PotDetailConverter;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.PotRecruitmentDetails;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.domain.mapping.PotApplication;
import stackpot.stackpot.domain.mapping.PotMember;
import stackpot.stackpot.repository.BadgeRepository.PotMemberBadgeRepository;
import stackpot.stackpot.repository.PotMemberRepository;
import stackpot.stackpot.repository.PotRepository.PotRecruitmentDetailsRepository;
import stackpot.stackpot.repository.PotRepository.PotRepository;
import stackpot.stackpot.repository.UserRepository.UserRepository;
import stackpot.stackpot.web.dto.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PotServiceImpl implements PotService {

    private final PotRepository potRepository;
    private final PotRecruitmentDetailsRepository recruitmentDetailsRepository;
    private final PotConverter potConverter;
    private final PotDetailConverter potDetailConverter;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PotMemberRepository potMemberRepository;
    private final PotMemberBadgeRepository potMemberBadgeRepository;
    private final UserTodoService userTodoService;
    private final MyPotConverter myPotConverter;


    @Transactional
  public PotResponseDto createPotWithRecruitments(PotRequestDto requestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new MemberHandler(ErrorStatus.AUTHENTICATION_FAILED);
        }
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        if (requestDto == null || requestDto.getRecruitmentDetails() == null) {
            throw new PotHandler(ErrorStatus._BAD_REQUEST);
        }
        // Pot ModeOfOperation 검증 추가
        if (!List.of("ONLINE", "OFFLINE", "HYBRID").contains(requestDto.getPotModeOfOperation())) {
            throw new PotHandler(ErrorStatus.INVALID_POT_MODE_OF_OPERATION);
        }
        Pot pot = potConverter.toEntity(requestDto, user);
        pot.setPotStatus("RECRUITING");
        Pot savedPot = potRepository.save(pot);

        List<PotRecruitmentDetails> recruitmentDetails = requestDto.getRecruitmentDetails().stream()
                .map(recruitmentDto -> {
                    try {
                        return PotRecruitmentDetails.builder()
                                .recruitmentRole(Role.valueOf(recruitmentDto.getRecruitmentRole()))
                                .recruitmentCount(recruitmentDto.getRecruitmentCount())
                                .pot(savedPot)
                                .build();
                    } catch (IllegalArgumentException e) {
                        throw new PotHandler(ErrorStatus.INVALID_ROLE);
                    }
                })
                .collect(Collectors.toList());
        recruitmentDetailsRepository.saveAll(recruitmentDetails);

        return potConverter.toDto(savedPot, recruitmentDetails);
    }

    @Transactional
    public PotResponseDto updatePotWithRecruitments(Long potId, PotRequestDto requestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new MemberHandler(ErrorStatus.AUTHENTICATION_FAILED);
        }
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        if (!pot.getUser().equals(user)) {
            throw new PotHandler(ErrorStatus.POT_FORBIDDEN);
        }

        if (requestDto == null) {
            throw new PotHandler(ErrorStatus._BAD_REQUEST);
        }
        // Pot ModeOfOperation 검증 추가
        if (!List.of("ONLINE", "OFFLINE", "HYBRID").contains(requestDto.getPotModeOfOperation())) {
            throw new PotHandler(ErrorStatus.INVALID_POT_MODE_OF_OPERATION);
        }
        pot.updateFields(Map.of(
                "potName", requestDto.getPotName(),
                "potDuration", requestDto.getPotDuration(),
                "potLan", requestDto.getPotLan(),
                "potContent", requestDto.getPotContent(),
                "potModeOfOperation", requestDto.getPotModeOfOperation(),
                "potSummary", requestDto.getPotSummary(),
                "recruitmentDeadline", requestDto.getRecruitmentDeadline()
        ));

        recruitmentDetailsRepository.deleteByPot_PotId(potId);

        List<PotRecruitmentDetails> recruitmentDetails = requestDto.getRecruitmentDetails().stream()
                .map(recruitmentDto -> {
                    try {
                        return PotRecruitmentDetails.builder()
                                .recruitmentRole(Role.valueOf(recruitmentDto.getRecruitmentRole()))
                                .recruitmentCount(recruitmentDto.getRecruitmentCount())
                                .pot(pot)
                                .build();
                    } catch (IllegalArgumentException e) {
                        throw new PotHandler(ErrorStatus.INVALID_ROLE);
                    }
                })
                .collect(Collectors.toList());
        recruitmentDetailsRepository.saveAll(recruitmentDetails);

        return potConverter.toDto(pot, recruitmentDetails);
    }

    @Transactional
    public void deletePot(Long potId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new MemberHandler(ErrorStatus.AUTHENTICATION_FAILED);
        }
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        if (!pot.getUser().equals(user)) {
            throw new PotHandler(ErrorStatus.POT_FORBIDDEN);
        }

        recruitmentDetailsRepository.deleteByPot_PotId(potId);
        potRepository.delete(pot);
    }

    @Transactional
    @Override
    public CursorPageResponse<CompletedPotResponseDto> getMyCompletedPots(Long cursor, int size) {
        // 현재 인증된 사용자 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new MemberHandler(ErrorStatus.AUTHENTICATION_FAILED);
        }
        String email = authentication.getName();

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 사용자가 참여하거나 생성한 COMPLETED 상태의 팟 가져오기
        List<Pot> pots = potRepository.findCompletedPotsCreatedByUser(user.getId(), cursor);


        // 커서 및 데이터 반환
        List<Pot> result = pots.size() > size ? pots.subList(0, size) : pots;
        Long nextCursor = result.isEmpty() ? null : result.get(result.size() - 1).getPotId();

        List<CompletedPotResponseDto> content = result.stream()
                .map(pot -> {
                    // 역할별 인원 수 조회 및 변환
                    List<Object[]> roleCounts = potMemberRepository.findRoleCountsByPotId(pot.getPotId());

                    Map<String, Integer> roleCountsMap = roleCounts.stream()
                            .collect(Collectors.toMap(
                                    roleCount -> ((Role) roleCount[0]).name(),
                                    roleCount -> ((Long) roleCount[1]).intValue()
                            ));

                    // 역할 정보를 "프론트엔드(2), 백엔드(1)" 형식으로 변환
                    String formattedMembers = roleCountsMap.entrySet().stream()
                            .map(entry -> getKoreanRoleName(entry.getKey()) + "(" + entry.getValue() + ")")
                            .collect(Collectors.joining(", "));

                    // 현재 사용자의 역할(Role) 결정
                    Role userPotRole;
                    if (pot.getUser().getId().equals(user.getId())) {
                        userPotRole = pot.getUser().getRole(); // Pot 생성자의 Role 반환
                    } else {
                        userPotRole = potMemberRepository.findRoleByUserId(pot.getPotId(), user.getId())
                                .orElse(pot.getUser().getRole());
                    }

                    // Pot -> CompletedPotResponseDto 변환
                    return potConverter.toCompletedPotResponseDto(pot, formattedMembers, userPotRole);
                })
                .collect(Collectors.toList());

        return new CursorPageResponse<>(content, nextCursor, pots.size() > size);
    }



//-------------------

    private final PotSummarizationService potSummarizationService;

    @Transactional
    @Override
    public List<PotPreviewResponseDto> getAllPots(Role role, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Pot> potPage = (role == null) ? potRepository.findAll(pageable) :
                potRepository.findByRecruitmentDetails_RecruitmentRole(role, pageable);

        return potPage.getContent().stream()
                .map(pot -> {
                    //  recruitmentDetails에서 role을 리스트로 변환하여 그대로 전달
                    List<String> roles = pot.getRecruitmentDetails().stream()
                            .map(recruitmentDetails -> String.valueOf(recruitmentDetails.getRecruitmentRole()))
                            .collect(Collectors.toList());

                    return potConverter.toPrviewDto(pot.getUser(), pot, roles);
                })
                .collect(Collectors.toList());
    }


    @Override
    public PotDetailResponseDto getPotDetails(Long potId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // Pot 조회
        Pot pot = potRepository.findPotWithRecruitmentDetailsByPotId(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        boolean isOwner = false;

        if(user.getId() == pot.getUser().getId()) isOwner = true;

        boolean isApplied = pot.getPotApplication().stream()
                .anyMatch(application -> application.getUser().getId().equals(user.getId()));

        // recruitmentDetails 리스트를 "FRONTEND(1), BACKEND(3)" 형태의 String으로 변환
        String recruitmentDetails = pot.getRecruitmentDetails().stream()
                .map(recruitmentDetail -> getKoreanRoleName(recruitmentDetail.getRecruitmentRole().name()) + "(" + recruitmentDetail.getRecruitmentCount() + ")")
                .collect(Collectors.joining(", "));

        // 변환기(PotDetailConverter) 사용

        return potDetailConverter.toPotDetailResponseDto(pot.getUser(), pot, recruitmentDetails, isOwner, isApplied);
    }

    // 특정 팟 지원자의 좋아요 상태 변경
    @Override
    public void patchLikes(Long potId, Long applicationId, Boolean liked) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        // 팟 생성자 확인
        if (!pot.getUser().getId().equals(user.getId())) {
            throw new PotHandler(ErrorStatus.POT_FORBIDDEN);
        }

        PotApplication application = pot.getPotApplication().stream()
                .filter(app -> app.getApplicationId().equals(applicationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Application not found with id: " + applicationId));

        application.setLiked(liked);
        potRepository.save(pot);
    }

    // 특정 팟의 좋아요한 지원자 목록 조회
    @Override
    public List<LikedApplicantResponseDTO> getLikedApplicants(Long potId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        // 팟 생성자 확인
        if (!pot.getUser().getId().equals(user.getId())) {
            throw new PotHandler(ErrorStatus.POT_FORBIDDEN);
        }

        return pot.getPotApplication().stream()
                .filter(PotApplication::getLiked)
                .map(app -> LikedApplicantResponseDTO.builder()
                        .applicationId(app.getApplicationId())
                        .applicantRole(app.getPotRole())
                        .potNickname(app.getUser().getNickname() + getVegetableNameByRole(String.valueOf(app.getPotRole())))
                        .liked(app.getLiked())
                        .build())
                .collect(Collectors.toList());
    }

    private String getVegetableNameByRole(String role) {
        Map<String, String> roleToVegetableMap = Map.of(
                "DESIGN", " 브로콜리",
                "PLANNING", " 당근",
                "BACKEND", " 양파",
                "FRONTEND", " 버섯"
        );

        return roleToVegetableMap.getOrDefault(role, "알 수 없음");
    }


    @Override
    public List<AppliedPotResponseDto> getAppliedPots() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 사용자가 지원한 팟 조회
        List<Pot> appliedPots = potRepository.findByPotApplication_User_Id(user.getId());
        if (appliedPots.isEmpty()) {
            throw new ApplicationHandler(ErrorStatus.APPLICATION_NOT_FOUND);
        }

        return appliedPots.stream()
                .map(pot -> {
                    // recruitmentDetails 리스트를 "프론트엔드(1), 백엔드(3)" 형태의 String으로 변환
                    String recruitmentDetails = pot.getRecruitmentDetails().stream()
                            .map(recruitmentDetail -> getKoreanRoleName(recruitmentDetail.getRecruitmentRole().name())
                                    + "(" + recruitmentDetail.getRecruitmentCount() + ")")
                            .collect(Collectors.joining(", "));

                    return potDetailConverter.toAppliedPotResponseDto(pot.getUser(), pot, recruitmentDetails);
                })
                .collect(Collectors.toList());
    }

    @Override
    public PotSummaryResponseDTO getPotSummary(Long potId) {
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        String prompt = "우리 프로젝트를 포트폴리오에 적합한 방식으로 400자로 요약해줘.\n" +
                "1. 프로젝트 개요: 해결하려는 문제, 목표\n" +
                "2. 주요 기능: 핵심적인 기능 설명\n" +
                "3. 기술 스택: 사용한 언어 및 프레임워크\n" +
                "4. 운영 방식: 온라인/오프라인 여부 및 협업 방식\n" +
                "5. 포트폴리오 적합성: 실무 경험, 팀워크, 기술 스택 습득 등의 강점 부각\n\n" +
                "프로젝트 정보:\n" +
                "- 프로젝트명: " + pot.getPotName() + "\n" +
                "- 내용: " + pot.getPotContent() + "\n" +
                "- 사용 기술: " + pot.getPotLan() + "\n" +
                "- 운영 방식: " + pot.getPotModeOfOperation();

        String summary = potSummarizationService.summarizeText(prompt, 400);

        return PotSummaryResponseDTO.builder()
                .summary(summary)
                .build();
    }

    @Transactional
    @Override
    public CursorPageResponse<CompletedPotResponseDto> getUserCompletedPots(Long userId, Long cursor, int size) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 사용자가 참여하거나 생성한 COMPLETED 상태의 Pot 조회 (커서 기반)
        List<Pot> pots = potRepository.findCompletedPotsByCursor(user.getId(), cursor);

        // 커서 및 데이터 반환
        List<Pot> result = pots.size() > size ? pots.subList(0, size) : pots;
        Long nextCursor = result.isEmpty() ? null : result.get(result.size() - 1).getPotId();

        // Pot -> DTO 변환
        List<CompletedPotResponseDto> content = result.stream()
                .map(pot -> {
                    //  역할별 참여자 수 조회
                    List<Object[]> roleCounts = potMemberRepository.findRoleCountsByPotId(pot.getPotId());
                    Map<String, Integer> roleCountsMap = roleCounts.stream()
                            .collect(Collectors.toMap(
                                    roleCount -> ((Role) roleCount[0]).name(),
                                    roleCount -> ((Long) roleCount[1]).intValue()
                            ));

                    //  역할 정보를 "프론트엔드(2), 백엔드(1)" 형식으로 변환
                    String formattedMembers = roleCountsMap.entrySet().stream()
                            .map(entry -> getKoreanRoleName(entry.getKey()) + "(" + entry.getValue() + ")")
                            .collect(Collectors.joining(", "));

                    // 현재 사용자의 역할(Role) 결정
                    Role userPotRole;
                    if (pot.getUser().getId().equals(user.getId())) {
                        userPotRole = pot.getUser().getRole(); // Pot 생성자의 Role 반환
                    } else {
                        userPotRole = potMemberRepository.findRoleByUserId(pot.getPotId(), user.getId())
                                .orElse(pot.getUser().getRole());
                    }

                    // Pot -> CompletedPotResponseDto 변환
                    return potConverter.toCompletedPotResponseDto(pot, formattedMembers, userPotRole);
                })
                .collect(Collectors.toList());

        // 반환 데이터 구성
        return new CursorPageResponse<>(content, nextCursor, pots.size() > size);
    }

    @Transactional
    @Override
    public void removeMemberFromPot(Long potId) {
        // 현재 로그인한 사용자 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new MemberHandler(ErrorStatus.AUTHENTICATION_FAILED);
        }
        String email = authentication.getName();

        // 현재 로그인한 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 팟 존재 여부 확인
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        // 팟 멤버 존재 여부 확인
        PotMember member = potMemberRepository.findByPotAndUser(pot, user)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND));

        // 팟 멤버 삭제
        potMemberRepository.delete(member);
    }

    @Transactional
    @Override
    public String removePotOrMember(Long potId) {
        // 현재 로그인한 사용자 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new MemberHandler(ErrorStatus.AUTHENTICATION_FAILED);
        }
        String email = authentication.getName();

        // 현재 로그인한 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 팟 존재 여부 확인
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        // 팟 생성자인지 확인
        if (pot.getUser().equals(user)) {
            // 팟 생성자일 경우 팟과 관련된 모든 데이터 삭제
            potRepository.delete(pot);
            return "팟이 성공적으로 삭제되었습니다.";
        } else {
            // 팟 멤버인지 확인
            PotMember member = potMemberRepository.findByPotAndUser(pot, user)
                    .orElseThrow(() -> new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND));

            // 팟 멤버 삭제
            potMemberRepository.delete(member);
            return "팟 멤버가 성공적으로 삭제되었습니다.";
        }
    }

    @Transactional
    @Override
    public PotResponseDto patchPotWithRecruitments(Long potId, PotRequestDto requestDto) {
        // 인증 정보에서 사용자 이메일 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 사용자 정보 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 팟 조회
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        // 소유자 확인
        if (!pot.getUser().equals(user)) {
            throw new PotHandler(ErrorStatus.POT_FORBIDDEN);
        }

        // 사용자의 온도 증가 (최대 100 제한)
        user.setUserTemperature(Math.min(user.getUserTemperature() + 5, 100));

        // pot.potMember들의 온도도 증가
        List<User> potMembers = pot.getPotMembers().stream()
                .map(PotMember::getUser)
                .collect(Collectors.toList());

        // 변경된 사용자 정보 저장
        userRepository.save(user);
        userRepository.saveAll(potMembers); // 모든 멤버 저장

        // 업데이트 로직
        pot.updateFields(Map.of(
                "potName", requestDto.getPotName(),
                //"potStartDate", requestDto.getPotStartDate(),
                    "potEndDate", LocalDateTime.now(),
                "potDuration", requestDto.getPotDuration(),
                "potLan", requestDto.getPotLan(),
                "potContent", requestDto.getPotContent(),
//                "potStatus", (requestDto.getPotStatus() != null ? requestDto.getPotStatus() : "COMPLETED"),
                "potModeOfOperation", requestDto.getPotModeOfOperation(),
                "potSummary", requestDto.getPotSummary(),
                "recruitmentDeadline", requestDto.getRecruitmentDeadline()
        ));

        // 기존 모집 정보 삭제
        recruitmentDetailsRepository.deleteByPot_PotId(potId);

        // 새로운 모집 정보 저장
        List<PotRecruitmentDetails> recruitmentDetails = requestDto.getRecruitmentDetails().stream()
                .map(recruitmentDto -> PotRecruitmentDetails.builder()
                        .recruitmentRole(Role.valueOf(recruitmentDto.getRecruitmentRole()))
                        .recruitmentCount(recruitmentDto.getRecruitmentCount())
                        .pot(pot)
                        .build())
                .collect(Collectors.toList());
        recruitmentDetailsRepository.saveAll(recruitmentDetails);

        pot.setPotStatus("COMPLETED");
        potRepository.save(pot); // 변경 사항 반영

        userTodoService.assignBadgeToTopMembers(potId);

        // DTO로 변환 후 반환
        return potConverter.toDto(pot, recruitmentDetails);
    }

    @Override
    public List<RecruitingPotResponseDto> getRecruitingPots() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        //  사용자가 만든 팟 중 'RECRUITING' 상태인 팟만 조회
        List<Pot> myRecruitingPots = potRepository.findByUserIdAndPotStatus(user.getId(), "RECRUITING");

        // DTO 변환을 리스트에 적용
        return myRecruitingPots.stream()
                .map(pot -> myPotConverter.convertToRecruitingPotResponseDto(pot, user.getId()))
                .collect(Collectors.toList());
    }
    @Transactional
    @Override
    public CompletedPotDetailResponseDto getCompletedPotDetail(Long potId, Long userId) {
        // 팟 조회
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        // 팟 상태 확인
        if (!"COMPLETED".equals(pot.getPotStatus())) {
            throw new PotHandler(ErrorStatus.INVALID_POT_STATUS);
        }

        // 특정 사용자 조회
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 특정 사용자의 팟 멤버 정보 조회
        PotMember potMember = potMemberRepository.findByPotAndUser(pot, targetUser)
                .orElse(null);

        // 어필 내용 가져오기
        String appealContent = (potMember != null) ? potMember.getAppealContent() : null;

        // 사용자의 역할(Role) 조회
        String userPotRole;
        if (pot.getUser().getId().equals(targetUser.getId())) {
            // 팟 멤버에 있지만 생성자인 경우 → 유저 테이블의 역할 사용
            userPotRole = getKoreanRoleName(targetUser.getRole().name());
        } else {
            // 팟 멤버에 있지만 생성자가 아닌 경우 → PotMember의 역할 사용
            userPotRole = getKoreanRoleName(potMember.getRoleName().name());
        }

        // DTO 반환
        return potDetailConverter.toCompletedPotDetailDto(pot, userPotRole, appealContent);
    }

    private String getKoreanRoleName(String role) {
        Map<String, String> roleToKoreaneMap = Map.of(
                "BACKEND", "백엔드",
                "FRONTEND", "프론트엔드",
                "DESIGN", "디자인",
                "PLANNING", "기획"
        );
        return roleToKoreaneMap.getOrDefault(role, "알 수 없음");
    }


}
