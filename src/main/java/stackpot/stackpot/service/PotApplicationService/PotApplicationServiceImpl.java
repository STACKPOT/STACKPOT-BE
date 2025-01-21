package stackpot.stackpot.service.PotApplicationService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import stackpot.stackpot.web.dto.PotApplicationRequestDto;
import stackpot.stackpot.web.dto.PotApplicationResponseDto;

import java.time.LocalDateTime;
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
        PotApplication savedApplication = potApplicationRepository.save(potApplication);

        // 저장된 지원 정보를 응답 DTO로 변환
        return potApplicationConverter.toDto(savedApplication);
    }


    @Override
    @Transactional(readOnly = true)
    public List<PotApplicationResponseDto> getApplicationsByPot(Long potId) {
        List<PotApplication> applications = potApplicationRepository.findByPot_PotId(potId);
        return applications.stream()
                .map(potApplicationConverter::toDto)
                .collect(Collectors.toList());
    }
}
