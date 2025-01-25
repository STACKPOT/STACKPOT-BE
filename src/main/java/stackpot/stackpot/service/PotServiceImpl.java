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
import stackpot.stackpot.config.security.JwtTokenProvider;
import stackpot.stackpot.converter.PotConverter;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.PotRecruitmentDetails;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.domain.mapping.PotApplication;
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

    @Transactional
    public PotResponseDto createPotWithRecruitments(PotRequestDto requestDto) {
        // 인증 정보에서 사용자 이메일 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 사용자 정보 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // 팟 생성
        Pot pot = potConverter.toEntity(requestDto, user);
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
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // 팟 조회
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("Pot not found with id: " + potId));

        // 소유자 확인
        if (!pot.getUser().equals(user)) {
            throw new IllegalArgumentException("You do not have permission to update this pot.");
        }

        // 업데이트 로직
        pot.updateFields(Map.of(
                "potName", requestDto.getPotName(),
                "potStartDate", requestDto.getPotStartDate(),
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
    public void deletePot(Long potId) {
        // 인증 정보에서 사용자 이메일 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 사용자 정보 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // 팟 조회
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("Pot not found with id: " + potId));

        // 팟 소유자 확인
        if (!pot.getUser().equals(user)) {
            throw new IllegalArgumentException("You do not have permission to delete this pot.");
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
                        .user(UserResponseDto.builder()
                                .nickname(pot.getUser().getNickname())
                                .role(String.valueOf(pot.getUser().getRole()))  // ENUM → String 변환
                                .build())
                        .pot(potConverter.toDto(pot, pot.getRecruitmentDetails()))  // 변환기 사용
                        .build())
                .collect(Collectors.toList());
    }


    @Override
    public ApplicantResponseDTO getPotDetails(Long potId) {
        Pot pot = potRepository.findPotWithRecruitmentDetailsByPotId(potId)
                .orElseThrow(() -> new IllegalArgumentException("Pot not found with id: " + potId));

        // 지원자 정보를 DTO로 변환
        List<ApplicantResponseDTO.ApplicantDto> applicantDto = pot.getPotApplication().stream()
                .map(app -> ApplicantResponseDTO.ApplicantDto.builder()
                        .applicationId(app.getApplicationId())
                        .potRole(String.valueOf(app.getPotRole()))
                        .liked(app.getLiked())
                        .build())
                .collect(Collectors.toList());

        // 모집 정보를 DTO로 변환
        List<PotRecruitmentResponseDto> recruitmentDetailsDto = pot.getRecruitmentDetails().stream()
                .map(details -> PotRecruitmentResponseDto.builder()
                        .recruitmentId(details.getRecruitmentId())
                        .recruitmentRole(String.valueOf(details.getRecruitmentRole()))
                        .recruitmentCount(details.getRecruitmentCount())
                        .build())
                .collect(Collectors.toList());

        // Pot 정보를 DTO로 변환
        PotResponseDto potDto = PotResponseDto.builder()
                .potId(pot.getPotId())
                .potName(pot.getPotName())
                .potStartDate(pot.getPotStartDate())
                .potEndDate(pot.getPotEndDate())
                .potDuration(pot.getPotDuration())
                .potLan(pot.getPotLan())
                .potContent(pot.getPotContent())
                .potStatus(pot.getPotStatus())
                .potSummary(pot.getPotSummary())
                .recruitmentDeadline(pot.getRecruitmentDeadline())
                .recruitmentDetails(recruitmentDetailsDto)
                .potModeOfOperation(pot.getPotModeOfOperation().name())
                .dDay(Math.toIntExact(ChronoUnit.DAYS.between(LocalDate.now(), pot.getRecruitmentDeadline())))
                .build();

        return ApplicantResponseDTO.builder()
                .user(UserResponseDto.builder()
                        .nickname(pot.getUser().getNickname())
                        .role(String.valueOf(pot.getUser().getRole()))
                        .build())
                .pot(potDto)
                .applicant(applicantDto)
                .build();
    }

    // 특정 팟 지원자의 좋아요 상태 변경
    @Override
    public void patchLikes(Long potId, Long applicationId, Boolean liked) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("Pot not found with id: " + potId));

        // 팟 생성자 확인
        if (!pot.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Only the pot owner can modify likes.");
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
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("Pot not found with id: " + potId));

        // 팟 생성자 확인
        if (!pot.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Only the pot owner can view liked applicants.");
        }

        return pot.getPotApplication().stream()
                .filter(PotApplication::getLiked)
                .map(app -> LikedApplicantResponseDTO.builder()
                        .applicationId(app.getApplicationId())
                        .applicantRole(String.valueOf(app.getPotRole()))
                        .potNickname(app.getUser().getNickname() + getVegetableNameByRole(String.valueOf(app.getPotRole())))
                        .liked(app.getLiked())
                        .build())
                .collect(Collectors.toList());
        /*pot role
            브로콜리 : 디자이너
            당근 : 기획자
            양파 : 백앤드
            버섯 : 프론트앤드
        */
    }

    private String getVegetableNameByRole(String role) {
        Map<String, String> roleToVegetableMap = Map.of(
                "디자이너", " 브로콜리",
                "기획자", " 당근",
                "백앤드", " 양파",
                "프론트앤드", " 버섯"
        );

        return roleToVegetableMap.getOrDefault(role, "알 수 없음");
    }


    @Override
    public List<PotAllResponseDTO.PotDetail> getAppliedPots() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // 사용자가 지원한 팟 조회
        List<Pot> appliedPots = potRepository.findByPotApplication_User_Id(user.getId());

        // Pot 리스트를 PotAllResponseDTO.PotDetail로 변환
        return appliedPots.stream().map(pot -> {
            // 모집 정보를 DTO로 변환
            List<PotRecruitmentResponseDto> recruitmentDetailsDto = pot.getRecruitmentDetails().stream()
                    .map(details -> PotRecruitmentResponseDto.builder()
                            .recruitmentId(details.getRecruitmentId())
                            .recruitmentRole(String.valueOf(details.getRecruitmentRole()))
                            .recruitmentCount(details.getRecruitmentCount())
                            .build())
                    .collect(Collectors.toList());

            // Pot 정보를 DTO로 변환
            PotResponseDto potDto = PotResponseDto.builder()
                    .potId(pot.getPotId())
                    .potName(pot.getPotName())
                    .potStartDate(pot.getPotStartDate())
                    .potEndDate(pot.getPotEndDate())
                    .potDuration(pot.getPotDuration())
                    .potLan(pot.getPotLan())
                    .potContent(pot.getPotContent())
                    .potStatus(pot.getPotStatus())
                    .potSummary(pot.getPotSummary())
                    .recruitmentDeadline(pot.getRecruitmentDeadline())
                    .recruitmentDetails(recruitmentDetailsDto)
                    .potModeOfOperation(pot.getPotModeOfOperation().name())
                    .dDay(Math.toIntExact(ChronoUnit.DAYS.between(LocalDate.now(), pot.getRecruitmentDeadline())))
                    .build();

            // 유저 정보를 DTO로 변환
            UserResponseDto userDto = UserResponseDto.builder()
                    .nickname(pot.getUser().getNickname())
                    .role(String.valueOf(pot.getUser().getRole()))
                    .build();

            return PotAllResponseDTO.PotDetail.builder()
                    .user(userDto)
                    .pot(potDto)
                    .build();
        }).collect(Collectors.toList());
    }

    // 사용자가 만든 팟 조회
    @Override
    public List<PotAllResponseDTO> getMyPots() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // 사용자가 만든 팟 조회
        List<Pot> myPots = potRepository.findByUserId(user.getId());

        // 모집중인 팟 리스트
        List<PotAllResponseDTO.PotDetail> recruitingPots = myPots.stream()
                .filter(pot -> "ongoing".equals(pot.getPotStatus()))
                .map(this::convertToPotDetail)
                .collect(Collectors.toList());

        // 진행 중인 팟 리스트 변환 (멤버 정보 포함)
        List<MyPotResponseDTO.OngoingPotsDetail> ongoingPots = myPots.stream()
                .filter(pot -> "recruiting".equals(pot.getPotStatus()))
                .map(this::convertToOngoingPotDetail)
                .collect(Collectors.toList());

        // 끓인 팟 리스트
        List<PotAllResponseDTO.PotDetail> completedPots = myPots.stream()
                .filter(pot -> "completed".equals(pot.getPotStatus()))
                .map(this::convertToPotDetail)
                .collect(Collectors.toList());

        return List.of(PotAllResponseDTO.builder()
                .recruitingPots(recruitingPots)
                .ongoingPots(ongoingPots)
                .completedPots(completedPots)
                .build());

    }

    @Override
    public void patchPotStatus(Long potId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("Pot not found with id: " + potId));

        // 팟 생성자 확인
        if (!pot.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Only the pot owner can modify pot status.");
        }

        // 팟 상태를 "complete"으로 변경
        pot.setPotStatus("complete");

        // 변경된 상태 저장
        potRepository.save(pot);
    }

    @Override
    public PotSummaryResponseDTO getPotSummary(Long potId) {
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("Pot not found with id: " + potId));

        String prompt = "구인글에 내용을 우리 프로젝트를 소개하는 400자로 정리해줘. " +
                "기획 배경, 주요기능, 어떤 언어와 프레임워크 사용했는지 등등 구체적인게 들어있으면 더 좋아.\n" +
                "내용: " + pot.getPotContent();

        String summary = potSummarizationService.summarizeText(prompt, 400);

        return PotSummaryResponseDTO.builder()
                .summary(summary)
                .build();
    }

    // Pot을 PotAllResponseDTO.PotDetail로 변환하는 메서드
    private PotAllResponseDTO.PotDetail convertToPotDetail(Pot pot) {
        List<PotRecruitmentResponseDto> recruitmentDetailsDto = pot.getRecruitmentDetails().stream()
                .map(details -> PotRecruitmentResponseDto.builder()
                        .recruitmentId(details.getRecruitmentId())
                        .recruitmentRole(String.valueOf(details.getRecruitmentRole()))
                        .recruitmentCount(details.getRecruitmentCount())
                        .build())
                .collect(Collectors.toList());

        PotResponseDto potDto = PotResponseDto.builder()
                .potId(pot.getPotId())
                .potName(pot.getPotName())
                .potStartDate(pot.getPotStartDate())
                .potEndDate(pot.getPotEndDate())
                .potDuration(pot.getPotDuration())
                .potLan(pot.getPotLan())
                .potContent(pot.getPotContent())
                .potStatus(pot.getPotStatus())
                .potSummary(pot.getPotSummary())
                .recruitmentDeadline(pot.getRecruitmentDeadline())
                .recruitmentDetails(recruitmentDetailsDto)
                .potModeOfOperation(pot.getPotModeOfOperation().name())
                .dDay(Math.toIntExact(ChronoUnit.DAYS.between(LocalDate.now(), pot.getRecruitmentDeadline())))
                .build();

        return PotAllResponseDTO.PotDetail.builder()
                .user(UserResponseDto.builder()
                        .nickname(pot.getUser().getNickname() + getVegetableNameByRole(String.valueOf(pot.getUser().getRole())))
                        .role(String.valueOf(pot.getUser().getRole()))
                        .build())
                .pot(potDto)
                .build();
    }

    // 진행 중인 팟 변환 메서드 (멤버 포함)
    private MyPotResponseDTO.OngoingPotsDetail convertToOngoingPotDetail(Pot pot) {
        List<PotRecruitmentResponseDto> recruitmentDetails = pot.getRecruitmentDetails().stream()
                .map(details -> PotRecruitmentResponseDto.builder()
                        .recruitmentId(details.getRecruitmentId())
                        .recruitmentRole(String.valueOf(details.getRecruitmentRole()))
                        .recruitmentCount(details.getRecruitmentCount())
                        .build())
                .collect(Collectors.toList());

        List<PotMemberResponseDTO> potMembers = pot.getPotMembers().stream()
                .map(member -> PotMemberResponseDTO.builder()
                        .potMemberId(member.getPotMemberId())
                        .roleName(String.valueOf(member.getRoleName()))
                        .build())
                .collect(Collectors.toList());

        return MyPotResponseDTO.OngoingPotsDetail.builder()
                .user(UserResponseDto.builder()
                        .nickname(pot.getUser().getNickname() + getVegetableNameByRole(String.valueOf(pot.getUser().getRole())))
                        .role(String.valueOf(pot.getUser().getRole()))
                        .build())
                .pot(PotResponseDto.builder()
                        .potId(pot.getPotId())
                        .potName(pot.getPotName())
                        .potStartDate(pot.getPotStartDate())
                        .potEndDate(pot.getPotEndDate())
                        .potStatus(pot.getPotStatus())
                        .recruitmentDetails(recruitmentDetails)
                        .build())
                .potMembers(potMembers)
                .build();
    }
}
