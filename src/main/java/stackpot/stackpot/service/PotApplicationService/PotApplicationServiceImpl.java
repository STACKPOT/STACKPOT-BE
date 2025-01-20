package stackpot.stackpot.service.PotApplicationService;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stackpot.stackpot.converter.PotApplicationConverter.PotApplicationConverter;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.enums.ApplicationStatus;
import stackpot.stackpot.domain.mapping.PotApplication;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.repository.PotApplicationRepository.PotApplicationRepository;
import stackpot.stackpot.repository.PotRepository.PotRepository;
import stackpot.stackpot.repository.UserRepository.UserRepository;
import stackpot.stackpot.config.security.JwtTokenProvider;
import stackpot.stackpot.web.dto.ApplicationRequestDto;
import stackpot.stackpot.web.dto.ApplicationResponseDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PotApplicationServiceImpl implements PotApplicationService {

    private final PotApplicationRepository potApplicationRepository;
    private final PotRepository potRepository;
    private final UserRepository userRepository;
    private final PotApplicationConverter potApplicationConverter;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public ApplicationResponseDto applyToPot(String token, Long potId, ApplicationRequestDto dto) {
        // 토큰에서 사용자 이메일 추출
        String email = jwtTokenProvider.getEmailFromToken(token);

        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 팟 조회
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팟을 찾을 수 없습니다."));

        // 중복 신청 방지
        boolean alreadyApplied = potApplicationRepository.existsByUserIdAndPot_PotId(user.getId(), potId);
        if (alreadyApplied) {
            throw new IllegalStateException("이미 해당 팟에 지원하셨습니다.");
        }

        // `PENDING` 상태로 지원 엔티티 생성
        PotApplication application = potApplicationConverter.toEntity(dto, pot, user);
        application.setApplicationStatus(ApplicationStatus.PENDING); // 상태를 명시적으로 설정

        // 지원 정보 저장
        PotApplication savedApplication = potApplicationRepository.save(application);

        // DTO로 변환하여 반환
        return potApplicationConverter.toDto(savedApplication);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> getApplicationsByPot(Long potId) {
        List<PotApplication> applications = potApplicationRepository.findByPot_PotId(potId);
        return applications.stream()
                .map(potApplicationConverter::toDto)
                .collect(Collectors.toList());
    }
}
