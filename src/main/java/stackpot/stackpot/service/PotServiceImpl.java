// Service
package stackpot.stackpot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
}
