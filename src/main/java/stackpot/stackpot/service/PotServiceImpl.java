package stackpot.stackpot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import stackpot.stackpot.config.security.JwtTokenProvider;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.mapping.PotApplication;
import stackpot.stackpot.repository.PotRepository.PotRepository;
import stackpot.stackpot.repository.UserRepository.UserRepository;
import stackpot.stackpot.web.dto.LikedApplicantResponseDTO;
import stackpot.stackpot.web.dto.PotResponseDTO;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PotServiceImpl implements PotService {

    private final PotRepository potRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    // JWT 토큰에서 사용자 ID 추출
    private Long extractUserIdFromToken(String token) {
        String jwtToken = token.replace("Bearer ", "");
        String email = jwtTokenProvider.getEmailFromToken(jwtToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        return user.getId();
    }

    @Override
    public List<PotResponseDTO> getAllPots(String role) {
        return potRepository.findByRecruitmentDetails_RecruitmentRole(role).stream()
                .map(pot -> PotResponseDTO.builder()
                        .user(PotResponseDTO.UserDto.builder()
                                .nickname(pot.getUser().getNickname())
                                .role(pot.getUser().getRole())
                                .build())
                        .pot(PotResponseDTO.PotDto.builder()
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
    public PotResponseDTO getPotDetails(Long potId) {
        Pot pot = potRepository.findPotWithRecruitmentDetailsByPotId(potId)
                .orElseThrow(() -> new IllegalArgumentException("Pot not found with id: " + potId));

        // 지원자 정보를 DTO로 변환
        List<PotResponseDTO.ApplicantDto> applicantDto = pot.getPotApplication().stream()
                .map(app -> PotResponseDTO.ApplicantDto.builder()
                        .applicationId(app.getApplicationId())
                        .potRole(app.getPotRole())
                        .liked(app.getLiked())
                        .build())
                .collect(Collectors.toList());

        // 모집 정보를 DTO로 변환
        List<PotResponseDTO.RecruitmentDetailsDto> recruitmentDetailsDto = pot.getRecruitmentDetails().stream()
                .map(details -> PotResponseDTO.RecruitmentDetailsDto.builder()
                        .recruitmentId(details.getRecruitmentId())
                        .recruitmentRole(details.getRecruitmentRole())
                        .recruitmentCount(details.getRecruitmentCount())
                        .build())
                .collect(Collectors.toList());

        // Pot 정보를 DTO로 변환
        PotResponseDTO.PotDto potDto = PotResponseDTO.PotDto.builder()
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

        return PotResponseDTO.builder()
                .user(PotResponseDTO.UserDto.builder()
                        .nickname(pot.getUser().getNickname())
                        .role(pot.getUser().getRole())
                        .build())
                .pot(potDto)
                .recruitmentDetails(recruitmentDetailsDto)
                .applicants(applicantDto)
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
    public List<PotResponseDTO> getAppliedPots() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // 사용자가 지원한 팟 조회
        List<Pot> appliedPots = potRepository.findByPotApplication_User_Id(user.getId());

        // Pot 리스트를 PotResponseDTO로 변환
        return appliedPots.stream().map(pot -> {
            // 모집 정보를 DTO로 변환
            List<PotResponseDTO.RecruitmentDetailsDto> recruitmentDetailsDto = pot.getRecruitmentDetails().stream()
                    .map(details -> PotResponseDTO.RecruitmentDetailsDto.builder()
                            .recruitmentId(details.getRecruitmentId())
                            .recruitmentRole(details.getRecruitmentRole())
                            .recruitmentCount(details.getRecruitmentCount())
                            .build())
                    .collect(Collectors.toList());

            // Pot 정보를 DTO로 변환
            PotResponseDTO.PotDto potDto = PotResponseDTO.PotDto.builder()
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

            return PotResponseDTO.builder()
                    .pot(potDto)
                    .recruitmentDetails(recruitmentDetailsDto)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public List<PotResponseDTO> getMyPots() {
        return List.of();
    }

}