// Service
package stackpot.stackpot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import stackpot.stackpot.converter.PotConverter;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.PotRecruitmentDetails;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.repository.PotRepository.PotRecruitmentDetailsRepository;
import stackpot.stackpot.repository.PotRepository.PotRepository;
import stackpot.stackpot.repository.UserRepository.UserRepository;
import stackpot.stackpot.service.PotService;
import stackpot.stackpot.web.dto.PotRequestDto;
import stackpot.stackpot.web.dto.PotResponseDto;
import stackpot.stackpot.config.security.JwtTokenProvider;
import org.springframework.security.core.context.SecurityContextHolder;
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
                        .recruitmentRole(recruitmentDto.getRecruitmentRole())
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
                        .recruitmentRole(recruitmentDto.getRecruitmentRole())
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

}
