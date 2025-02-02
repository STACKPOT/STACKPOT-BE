package stackpot.stackpot.service.PotApplicationService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.PotHandler;
import stackpot.stackpot.config.security.JwtTokenProvider;
import stackpot.stackpot.converter.PotApplicationConverter.PotApplicationConverter;
import stackpot.stackpot.converter.PotDetailConverter;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.ApplicationStatus;
import stackpot.stackpot.domain.mapping.PotApplication;
import stackpot.stackpot.repository.PotApplicationRepository.PotApplicationRepository;
import stackpot.stackpot.repository.PotRepository.PotRepository;
import stackpot.stackpot.repository.UserRepository.UserRepository;
import stackpot.stackpot.service.EmailService.EmailService;
import stackpot.stackpot.web.dto.PotApplicationRequestDto;
import stackpot.stackpot.web.dto.PotApplicationResponseDto;
import stackpot.stackpot.web.dto.PotDetailResponseDto;
import stackpot.stackpot.web.dto.PotDetailWithApplicantsResponseDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PotApplicationServiceImpl implements PotApplicationService {

    private final PotApplicationRepository potApplicationRepository;
    private final PotRepository potRepository;
    private final UserRepository userRepository;
    private final PotApplicationConverter potApplicationConverter;
    private final PotDetailConverter potDetailConverter;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    @Transactional
    public PotApplicationResponseDto applyToPot(PotApplicationRequestDto dto, Long potId) {
        // 인증된 사용자 이메일 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 팟 조회
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팟을 찾을 수 없습니다."));

        // 중복 지원 방지
        if (potApplicationRepository.existsByUserIdAndPot_PotId(user.getId(), potId)) {
            throw new IllegalStateException("이미 해당 팟에 지원하셨습니다.");
        }

        // 지원 엔티티 생성 및 저장
        PotApplication potApplication = potApplicationConverter.toEntity(dto, pot, user);
        potApplication.setApplicationStatus(ApplicationStatus.PENDING);
        potApplication.setAppliedAt(LocalDateTime.now());

        PotApplication savedApplication = potApplicationRepository.save(potApplication);

        // 이메일 전송
        // 이메일 전송
        emailService.sendSupportNotification(
                pot.getUser().getEmail(),
                pot.getPotName(),
                user.getNickname(),
                user.getUserIntroduction() // 한 줄 소개 추가
        );

        // 저장된 지원 정보를 응답 DTO로 변환
        return potApplicationConverter.toDto(savedApplication);
    }



    @Override
    @Transactional(readOnly = true)
    public List<PotApplicationResponseDto> getApplicantsByPotId(Long potId) {
        // 현재 인증된 사용자 이메일 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 사용자 정보 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
        // 팟 조회
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팟을 찾을 수 없습니다."));



        // 소유자 확인
        if (!pot.getUser().equals(user)) {
            throw new IllegalArgumentException("해당 팟 지원자 목록을 볼 수 있는 권한이 없습니다.");
        }

        // 지원자 목록 조회
        List<PotApplication> applications = potApplicationRepository.findByPot_PotId(potId);

        // DTO 변환 후 반환
        return applications.stream()
                .map(potApplicationConverter::toDto)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional(readOnly = true)
    public PotDetailWithApplicantsResponseDto getPotDetailsAndApplicants(Long potId) {
        // 현재 인증된 사용자 이메일 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 사용자 정보 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // Pot 조회 (지원자 목록을 가져오기 위해 연관된 엔티티까지 페치 조인)
        Pot pot = potRepository.findPotWithRecruitmentDetailsByPotId(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        // 모집 세부 사항 변환 ("FRONTEND(1), BACKEND(3)" 형태)
        String recruitmentDetails = pot.getRecruitmentDetails().stream()
                .map(recruitmentDetail -> getKoreanRoleName(recruitmentDetail.getRecruitmentRole().name()) + "(" + recruitmentDetail.getRecruitmentCount() + ")")
                .collect(Collectors.joining(", "));

        // Pot 상세 DTO 변환
        PotDetailResponseDto potDetailDto = potDetailConverter.toPotDetailResponseDto(pot.getUser(), pot, recruitmentDetails);

        // 지원자 목록 조회 조건: 사용자가 소유자 && Pot의 status가 RECRUITING일 때만 조회
        List<PotApplicationResponseDto> applicants = Collections.emptyList(); // 기본값: 빈 리스트
        if (pot.getUser().equals(user) && "RECRUITING".equals(pot.getPotStatus())) {

            List<PotApplication> applications = potApplicationRepository.findByPot_PotId(potId);
            applicants = applications.stream()
                    .map(potApplicationConverter::toDto)
                    .collect(Collectors.toList());
        }

        // 최종 DTO 반환
        return PotDetailWithApplicantsResponseDto.builder()
                .potDetail(potDetailDto)
                .applicants(applicants)
                .build();
    }

    private String getKoreanRoleName(String role) {
        Map<String, String> roleToKoreaneMap = Map.of(
                "BACKEND", " 백앤드",
                "FRONTEND", " 프론트앤드",
                "DESIGN", " 디자인",
                "PLANNING", " 기획"
        );
        return roleToKoreaneMap.getOrDefault(role, "알 수 없음");
    }

}
