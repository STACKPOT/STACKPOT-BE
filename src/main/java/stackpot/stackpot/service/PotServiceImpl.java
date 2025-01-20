// Service
package stackpot.stackpot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import stackpot.stackpot.config.security.JwtTokenProvider;
import stackpot.stackpot.converter.PotConverter;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.PotRecruitmentDetails;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.mapping.PotApplication;
import stackpot.stackpot.repository.PotRepository.PotRecruitmentDetailsRepository;
import stackpot.stackpot.repository.PotRepository.PotRepository;
import stackpot.stackpot.repository.UserRepository.UserRepository;
import stackpot.stackpot.web.dto.*;
import stackpot.stackpot.service.PotService;
import stackpot.stackpot.web.dto.PotRequestDto;
import stackpot.stackpot.web.dto.PotResponseDto;
import stackpot.stackpot.config.security.JwtTokenProvider;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
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

    @Override
    @Transactional
    public PotResponseDto createPotWithRecruitments(String token, PotRequestDto requestDto) {
        // JWT에서 이메일 추출
        String email = jwtTokenProvider.getEmailFromToken(token);

        // 2. 이메일로 사용자 로드
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 3. Pot 엔티티 생성 및 사용자 설정
        Pot pot = potConverter.toEntity(requestDto, user);
        pot.setUser(user); // 사용자 정보 설정
        // 4. Pot 저장
        Pot savedPot = potRepository.save(pot);
        // RecruitmentDetails 저장 로직
        List<PotRecruitmentDetails> recruitmentDetails = requestDto.getRecruitmentDetails().stream()
                .map(recruitmentDto -> PotRecruitmentDetails.builder()
                        .recruitmentRole(recruitmentDto.getRecruitmentRole())
                        .recruitmentCount(recruitmentDto.getRecruitmentCount())
                        .pot(savedPot)
                        .build())
                .collect(Collectors.toList());

        recruitmentDetailsRepository.saveAll(recruitmentDetails);

        // Convert and return response DTO
        return potConverter.toDto(savedPot, recruitmentDetails);

    }
    @Transactional
    public PotResponseDto updatePotWithRecruitments(String token, Long potId, PotRequestDto requestDto) {
        // JWT에서 사용자 이메일 추출
        String email = jwtTokenProvider.getEmailFromToken(token);

        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 팟 조회
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 팟을 찾을 수 없습니다."));

        // 권한 확인
        if (!pot.getUser().equals(user)) {
            throw new IllegalArgumentException("해당 팟을 수정할 권한이 없습니다.");
        }

        // 기존 모집 정보 삭제
        recruitmentDetailsRepository.deleteByPot_PotId(potId);

        // 새로운 정보로 업데이트
        Map<String, Object> updates = new HashMap<>();
        updates.put("potName", requestDto.getPotName());
        updates.put("potStartDate", requestDto.getPotStartDate());
        updates.put("potEndDate", requestDto.getPotEndDate());
        updates.put("potDuration", requestDto.getPotDuration());
        updates.put("potLan", requestDto.getPotLan());
        updates.put("potContent", requestDto.getPotContent());
        updates.put("potStatus", requestDto.getPotStatus());
        updates.put("potModeOfOperation", requestDto.getPotModeOfOperation());
        updates.put("potSummary", requestDto.getPotSummary());
        updates.put("recruitmentDeadline", requestDto.getRecruitmentDeadline());
        pot.updateFields(updates);

        // 새로운 모집 정보 추가
        List<PotRecruitmentDetails> recruitmentDetails = requestDto.getRecruitmentDetails().stream()
                .map(recruitmentDto -> PotRecruitmentDetails.builder()
                        .recruitmentRole(recruitmentDto.getRecruitmentRole())
                        .recruitmentCount(recruitmentDto.getRecruitmentCount())
                        .pot(pot)
                        .build())
                .collect(Collectors.toList());
        recruitmentDetailsRepository.saveAll(recruitmentDetails);

        // 수정된 데이터 반환
        return potConverter.toDto(pot, recruitmentDetails);
    }


    @Override
    @Transactional
    public void deletePot(String token, Long potId) {
        // JWT에서 이메일 추출
        String email = jwtTokenProvider.getEmailFromToken(token);

        // 이메일로 사용자 로드
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 팟 로드
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 팟이 존재하지 않습니다."));

        // 사용자가 팟의 생성자인지 확인
        if (!pot.getUser().equals(user)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        // 연관된 모집 정보 삭제
        recruitmentDetailsRepository.deleteByPot_PotId(potId);

        // 팟 삭제
        potRepository.delete(pot);
    }

    //-----------------

    private final PotRepository potRepository;
    private final UserRepository userRepository;
    private final PotSummarizationService potSummarizationService;

    @Override
    public List<PotAllResponseDTO.PotDetail> getAllPots(String role) {
        List<Pot> pots;

        if (role == null || role.isEmpty()) {
            pots = potRepository.findAll();  // 모든 팟 조회
        } else {
            pots = potRepository.findByRecruitmentDetails_RecruitmentRole(role);
        }

        return pots.stream()
                .map(pot -> PotAllResponseDTO.PotDetail.builder()
                        .user(UserResponseDTO.builder()
                                .nickname(pot.getUser().getNickname())
                                .role(pot.getUser().getRole())
                                .build())
                        .pot(PotResponseDTO.builder()
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
                                .potModeOfOperation(pot.getPotModeOfOperation().name())
                                .dDay(Math.toIntExact(ChronoUnit.DAYS.between(LocalDate.now(), pot.getRecruitmentDeadline())))
                                .build())
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
                        .potRole(app.getPotRole())
                        .liked(app.getLiked())
                        .build())
                .collect(Collectors.toList());

        // 모집 정보를 DTO로 변환
        List<RecruitmentDetailsResponseDTO> recruitmentDetailsDto = pot.getRecruitmentDetails().stream()
                .map(details -> RecruitmentDetailsResponseDTO.builder()
                        .recruitmentId(details.getRecruitmentId())
                        .recruitmentRole(details.getRecruitmentRole())
                        .recruitmentCount(details.getRecruitmentCount())
                        .build())
                .collect(Collectors.toList());

        // Pot 정보를 DTO로 변환
        PotResponseDTO potDto = PotResponseDTO.builder()
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
                .potModeOfOperation(pot.getPotModeOfOperation().name())
                .dDay(Math.toIntExact(ChronoUnit.DAYS.between(LocalDate.now(), pot.getRecruitmentDeadline())))
                .build();

        return ApplicantResponseDTO.builder()
                .user(UserResponseDTO.builder()
                        .nickname(pot.getUser().getNickname())
                        .role(pot.getUser().getRole())
                        .build())
                .pot(potDto)
                .recruitmentDetails(recruitmentDetailsDto)
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
                        .applicantRole(app.getPotRole())
                        .potNickname(app.getUser().getNickname() + " - " + app.getPotRole())
                        .liked(app.getLiked())
                        .build())
                .collect(Collectors.toList());
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
            List<RecruitmentDetailsResponseDTO> recruitmentDetailsDto = pot.getRecruitmentDetails().stream()
                    .map(details -> RecruitmentDetailsResponseDTO.builder()
                            .recruitmentId(details.getRecruitmentId())
                            .recruitmentRole(details.getRecruitmentRole())
                            .recruitmentCount(details.getRecruitmentCount())
                            .build())
                    .collect(Collectors.toList());

            // Pot 정보를 DTO로 변환
            PotResponseDTO potDto = PotResponseDTO.builder()
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
                    .potModeOfOperation(pot.getPotModeOfOperation().name())
                    .dDay(Math.toIntExact(ChronoUnit.DAYS.between(LocalDate.now(), pot.getRecruitmentDeadline())))
                    .build();

            // 유저 정보를 DTO로 변환
            UserResponseDTO userDto = UserResponseDTO.builder()
                    .nickname(pot.getUser().getNickname())
                    .role(pot.getUser().getRole())
                    .build();

            return PotAllResponseDTO.PotDetail.builder()
                    .user(userDto)
                    .pot(potDto)
                    .recruitmentDetails(recruitmentDetailsDto)
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
                .filter(pot -> "모집중".equals(pot.getPotStatus()))
                .map(this::convertToPotDetail)
                .collect(Collectors.toList());

        // 진행 중인 팟 리스트 변환 (멤버 정보 포함)
        List<MyPotResponseDTO.OngoingPotsDetail> ongoingPots = myPots.stream()
                .filter(pot -> "진행중".equals(pot.getPotStatus()))
                .map(this::convertToOngoingPotDetail)
                .collect(Collectors.toList());

        // 끓인 팟 리스트
        List<PotAllResponseDTO.PotDetail> completedPots = myPots.stream()
                .filter(pot -> "끓임".equals(pot.getPotStatus()))
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
        List<RecruitmentDetailsResponseDTO> recruitmentDetailsDto = pot.getRecruitmentDetails().stream()
                .map(details -> RecruitmentDetailsResponseDTO.builder()
                        .recruitmentId(details.getRecruitmentId())
                        .recruitmentRole(details.getRecruitmentRole())
                        .recruitmentCount(details.getRecruitmentCount())
                        .build())
                .collect(Collectors.toList());

        PotResponseDTO potDto = PotResponseDTO.builder()
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
                .potModeOfOperation(pot.getPotModeOfOperation().name())
                .dDay(Math.toIntExact(ChronoUnit.DAYS.between(LocalDate.now(), pot.getRecruitmentDeadline())))
                .build();

        return PotAllResponseDTO.PotDetail.builder()
                .user(UserResponseDTO.builder()
                        .nickname(pot.getUser().getNickname())
                        .role(pot.getUser().getRole())
                        .build())
                .pot(potDto)
                .recruitmentDetails(recruitmentDetailsDto)
                .build();
    }

    // 진행 중인 팟 변환 메서드 (멤버 포함)
    private MyPotResponseDTO.OngoingPotsDetail convertToOngoingPotDetail(Pot pot) {
        List<RecruitmentDetailsResponseDTO> recruitmentDetails = pot.getRecruitmentDetails().stream()
                .map(details -> RecruitmentDetailsResponseDTO.builder()
                        .recruitmentId(details.getRecruitmentId())
                        .recruitmentRole(details.getRecruitmentRole())
                        .recruitmentCount(details.getRecruitmentCount())
                        .build())
                .collect(Collectors.toList());

        List<PotMemberResponseDTO> potMembers = pot.getPotMembers().stream()
                .map(member -> PotMemberResponseDTO.builder()
                        .potMemberId(member.getPotMemberId())
                        .roleName(member.getRoleName())

                        .build())
                .collect(Collectors.toList());

        return MyPotResponseDTO.OngoingPotsDetail.builder()
                .user(UserResponseDTO.builder()
                        .nickname(pot.getUser().getNickname())
                        .role(pot.getUser().getRole())
                        .build())
                .pot(PotResponseDTO.builder()
                        .potId(pot.getPotId())
                        .potName(pot.getPotName())
                        .potStartDate(pot.getPotStartDate())
                        .potEndDate(pot.getPotEndDate())
                        .potStatus(pot.getPotStatus())
                        .build())
                .recruitmentDetails(recruitmentDetails)
                .potMembers(potMembers)
                .build();
    }


}
