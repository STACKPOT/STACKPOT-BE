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
import stackpot.stackpot.converter.PotConverter;
import stackpot.stackpot.converter.UserConverter;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.PotRecruitmentDetails;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.domain.mapping.PotApplication;
import stackpot.stackpot.domain.mapping.PotMember;
import stackpot.stackpot.repository.PotMemberRepository;
import stackpot.stackpot.repository.PotRepository.PotRecruitmentDetailsRepository;
import stackpot.stackpot.repository.PotRepository.PotRepository;
import stackpot.stackpot.repository.UserRepository.UserRepository;
import stackpot.stackpot.web.dto.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PotServiceImpl implements PotService {

    private final PotRepository potRepository;
    private final PotRecruitmentDetailsRepository recruitmentDetailsRepository;
    private final PotConverter potConverter;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PotMemberRepository potMemberRepository;
    @Transactional
    public PotResponseDto createPotWithRecruitments(PotRequestDto requestDto) {
        // 인증 정보에서 사용자 이메일 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 사용자 정보 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 팟 생성
        Pot pot = potConverter.toEntity(requestDto, user);
        // 2. 팟 상태를 "ing"로 설정
        pot.setPotStatus("RECRUITING");
        Pot savedPot = potRepository.save(pot);

        // 모집 정보 저장
        List<PotRecruitmentDetails> recruitmentDetails = requestDto.getRecruitmentDetails().stream()
                .map(recruitmentDto -> PotRecruitmentDetails.builder()
                        .recruitmentRole(Role.valueOf(recruitmentDto.getRecruitmentRole()))
                        .recruitmentCount(recruitmentDto.getRecruitmentCount())
                        .pot(savedPot)
                        .build())
                .collect(Collectors.toList());
        recruitmentDetailsRepository.saveAll(recruitmentDetails);

        // DTO로 변환 후 반환
        return potConverter.toDto(savedPot, recruitmentDetails);
    }

    @Transactional
    @Override
    public PotResponseDto updatePotWithRecruitments(Long potId, PotRequestDto requestDto) {
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

        // 업데이트 로직
        pot.updateFields(Map.of(
                "potName", requestDto.getPotName(),
//                "potStartDate", requestDto.getPotStartDate(),
                "potEndDate", requestDto.getPotEndDate(),
                "potDuration", requestDto.getPotDuration(),
                "potLan", requestDto.getPotLan(),
                "potContent", requestDto.getPotContent(),
                "potStatus", requestDto.getPotStatus(),
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

        // DTO로 변환 후 반환
        return potConverter.toDto(pot, recruitmentDetails);
    }

    @Transactional
    @Override
    public CursorPageResponse<CompletedPotResponseDto> getMyCompletedPots(Long cursor, int size) {
        // 현재 인증된 사용자 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 사용자가 참여하거나 생성한 COMPLETED 상태의 팟 가져오기
        List<Pot> pots = potRepository.findCompletedPotsByCursor(user.getId(), cursor);

        // 커서 및 데이터 반환
        List<Pot> result = pots.size() > size ? pots.subList(0, size) : pots;
        Long nextCursor = result.isEmpty() ? null : result.get(result.size() - 1).getPotId();

        List<CompletedPotResponseDto> content = result.stream()
                .map(pot -> {
                    List<Object[]> roleCounts = potMemberRepository.findRoleCountsByPotId(pot.getPotId());
                    Map<String, Integer> roleCountsMap = roleCounts.stream()
                            .collect(Collectors.toMap(
                                    roleCount -> ((Role) roleCount[0]).name(),
                                    roleCount -> ((Long) roleCount[1]).intValue()
                            ));
                    return potConverter.toCompletedPotResponseDto(pot, roleCountsMap);
                })
                .collect(Collectors.toList());

        return new CursorPageResponse<>(content, nextCursor, pots.size() > size);
    }

        @Transactional
    public void deletePot(Long potId) {
        // 인증 정보에서 사용자 이메일 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 사용자 정보 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 팟 조회
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        // 팟 소유자 확인
        if (!pot.getUser().equals(user)) {
            throw new PotHandler(ErrorStatus.POT_FORBIDDEN);
        }

        // 모집 정보 삭제
        recruitmentDetailsRepository.deleteByPot_PotId(potId);

        // 팟 삭제
        potRepository.delete(pot);
    }


//-------------------

    private final PotSummarizationService potSummarizationService;

    @Transactional
    @Override
    public List<PotAllResponseDTO.PotDetail> getAllPots(Role role, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Pot> potPage;

        if (role == null) {
            potPage = potRepository.findAll(pageable);
        } else {
            potPage = potRepository.findByRecruitmentDetails_RecruitmentRole(role, pageable);
        }

        return potPage.getContent().stream()
                .map(pot -> PotAllResponseDTO.PotDetail.builder()
                        .user(UserConverter.toDto(pot.getUser()))
                        .pot(potConverter.toDto(pot, pot.getRecruitmentDetails()))  // 변환기 사용
                        .build())
                .collect(Collectors.toList());
    }


    @Override
    public ApplicantResponseDTO getPotDetails(Long potId) {
        Pot pot = potRepository.findPotWithRecruitmentDetailsByPotId(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        // 지원자 정보를 DTO로 변환
        List<ApplicantResponseDTO.ApplicantDto> applicantDto = pot.getPotApplication().stream()
                .map(app -> ApplicantResponseDTO.ApplicantDto.builder()
                        .applicationId(app.getApplicationId())
                        .potRole(String.valueOf(app.getPotRole()))
                        .liked(app.getLiked())
                        .build())
                .collect(Collectors.toList());


        return ApplicantResponseDTO.builder()
                .user(UserConverter.toDto(pot.getUser()))
                .pot(potConverter.toDto(pot, pot.getRecruitmentDetails()))  // 변환기 사용
                .applicant(applicantDto)
                .build();
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
    public List<PotAllResponseDTO.PotDetail> getAppliedPots() {
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
                .map(pot -> PotAllResponseDTO.PotDetail.builder()
                        .user(UserConverter.toDto(pot.getUser()))
                        .pot(potConverter.toDto(pot, pot.getRecruitmentDetails()))
                        .build()
                )
                .collect(Collectors.toList());
    }

    // 사용자가 만든 팟 조회
    @Override
    public List<PotAllResponseDTO> getMyPots() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 사용자가 만든 팟 조회
        List<Pot> myPots = potRepository.findByUserId(user.getId());

        // 모집중인 팟 리스트 (recruiting 상태 필터링)
        List<PotAllResponseDTO.PotDetail> recruitingPots = myPots.stream()
                .filter(pot -> "RECRUITING".equalsIgnoreCase(pot.getPotStatus()))  // 소문자 비교
                .map(this::convertToPotDetail)
                .collect(Collectors.toList());

        // 진행 중인 팟 리스트 (ongoing 상태 필터링)
        List<MyPotResponseDTO.OngoingPotsDetail> ongoingPots = myPots.stream()
                .filter(pot -> "ONGOING".equalsIgnoreCase(pot.getPotStatus()))  // 소문자 비교
                .map(this::convertToOngoingPotDetail)
                .collect(Collectors.toList());

        // 끓인 팟 리스트 (COMPLETED 상태 필터링)
        List<CompletedPotResponseDto> completedPots = myPots.stream()
                .filter(pot -> "COMPLETED".equalsIgnoreCase(pot.getPotStatus()))
                .map(pot -> {
                    List<Object[]> roleCounts = potMemberRepository.findRoleCountsByPotId(pot.getPotId());
                    Map<String, Integer> roleCountsMap = roleCounts.stream()
                            .collect(Collectors.toMap(
                                    roleCount -> ((Role) roleCount[0]).name(),
                                    roleCount -> ((Long) roleCount[1]).intValue()
                            ));
                    return potConverter.toCompletedPotResponseDto(pot, roleCountsMap);
                })
                .collect(Collectors.toList());

        return List.of(PotAllResponseDTO.builder()
                .recruitingPots(recruitingPots)
                .ongoingPots(ongoingPots)
                .completedPots(completedPots) // 끓인 팟을 CompletedPotResponseDto로 반환
                .build());

    }

    @Override
    public PotSummaryResponseDTO getPotSummary(Long potId) {
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        String prompt = "구인글에 내용을 우리 프로젝트를 소개하는 400자로 정리해줘. " +
                "기획 배경, 주요기능, 어떤 언어와 프레임워크 사용했는지 등등 구체적인게 들어있으면 더 좋아.\n" +
                "내용: " + pot.getPotContent();

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
                    // 역할별 참여자 수 조회
                    List<Object[]> roleCounts = potMemberRepository.findRoleCountsByPotId(pot.getPotId());
                    Map<String, Integer> roleCountsMap = roleCounts.stream()
                            .collect(Collectors.toMap(
                                    roleCount -> ((Role) roleCount[0]).name(),
                                    roleCount -> ((Long) roleCount[1]).intValue()
                            ));

                    // Pot -> CompletedPotResponseDto 변환
                    return potConverter.toCompletedPotResponseDto(pot, roleCountsMap);
                })
                .collect(Collectors.toList());

        // 반환 데이터 구성
        return new CursorPageResponse<>(content, nextCursor, pots.size() > size);
    }

    // Pot을 PotAllResponseDTO.PotDetail로 변환하는 메서드
    private PotAllResponseDTO.PotDetail convertToPotDetail(Pot pot) {

        return PotAllResponseDTO.PotDetail.builder()
                .user(UserConverter.toDto(pot.getUser()))
                .pot(potConverter.toDto(pot, pot.getRecruitmentDetails()))  // 변환기 사용
                .build();
    }

    // 진행 중인 팟 변환 메서드 (멤버 포함)
    private MyPotResponseDTO.OngoingPotsDetail convertToOngoingPotDetail(Pot pot) {

        List<PotMemberResponseDTO> potMembers = pot.getPotMembers().stream()
                .map(member -> PotMemberResponseDTO.builder()
                        .potMemberId(member.getPotMemberId())
                        .roleName(member.getRoleName())
                        .build())
                .collect(Collectors.toList());


        return MyPotResponseDTO.OngoingPotsDetail.builder()
                .user(UserConverter.toDto(pot.getUser()))
                .pot(potConverter.toDto(pot, pot.getRecruitmentDetails()))  // 변환기 사용
                .potMembers(potMembers)
                .build();
    }
    @Transactional
    @Override
    public void removeMemberFromPot(Long potId) {
        // 현재 로그인한 사용자 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 현재 로그인한 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("현재 사용자를 찾을 수 없습니다."));

        // 팟 존재 여부 확인
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팟을 찾을 수 없습니다."));

        // 팟 멤버 존재 여부 확인
        PotMember member = potMemberRepository.findByPotAndUser(pot, user)
                .orElseThrow(() -> new IllegalArgumentException("해당 팟에 사용자가 존재하지 않습니다."));

        // 팟 멤버 삭제
        potMemberRepository.delete(member);
    }


    @Transactional
    @Override
    public String removePotOrMember(Long potId) {
        // 현재 로그인한 사용자 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 현재 로그인한 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("현재 사용자를 찾을 수 없습니다."));

        // 팟 존재 여부 확인
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팟을 찾을 수 없습니다."));

        // 팟 생성자인지 확인
        if (pot.getUser().equals(user)) {
            // 팟 생성자일 경우 팟과 관련된 모든 데이터 삭제
            potRepository.delete(pot);
            return "팟이 성공적으로 삭제되었습니다.";
        } else {
            // 팟 멤버인지 확인
            PotMember member = potMemberRepository.findByPotAndUser(pot, user)
                    .orElseThrow(() -> new IllegalArgumentException("해당 팟에 사용자가 존재하지 않습니다."));

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
//                "potStartDate", requestDto.getPotStartDate(),
                "potEndDate", requestDto.getPotEndDate(),
                "potDuration", requestDto.getPotDuration(),
                "potLan", requestDto.getPotLan(),
                "potContent", requestDto.getPotContent(),
                "potStatus", requestDto.getPotStatus(),
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

        // DTO로 변환 후 반환
        return potConverter.toDto(pot, recruitmentDetails);
    }

}
