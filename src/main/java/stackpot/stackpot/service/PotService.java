package stackpot.stackpot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.repository.PotRepository;
import stackpot.stackpot.web.dto.PotResponseDto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PotService {

    private final PotRepository potRepository;

    public List<PotResponseDto> getAllPots(String role) {
        return potRepository.findByRecruitmentRole(role).stream()
                .map(pot -> PotResponseDto.builder()
                        .user(PotResponseDto.UserDto.builder()
                                .nickname(pot.getUser().getNickname())
                                .role(pot.getUser().getRole())
                                .build())
                        .pot(PotResponseDto.PotDto.builder()
                                .potId(pot.getPotId())
                                .potName(pot.getPotName())
                                .potContent(pot.getPotContent())
                                .recruitmentDeadline(pot.getRecruitmentDeadline())
                                .dDay(ChronoUnit.DAYS.between(LocalDate.now(), pot.getRecruitmentDeadline()))
                                .build())
                        .build())
                .collect(Collectors.toList());
    }

    // 특정 Pot의 상세 정보 조회
    public PotResponseDto getPotDetails(Long potId) {
        Pot pot = potRepository.findPotWithRecruitmentDetailsById(potId)
                .orElseThrow(() -> new IllegalArgumentException("Pot not found with id: " + potId));

        return PotResponseDto.builder()
                .user(PotResponseDto.UserDto.builder()
                        .nickname(pot.getUser().getNickname())
                        .role(pot.getUser().getRole())
                        .build())
                .pot(PotResponseDto.PotDto.builder()
                        .potId(pot.getPotId())
                        .potName(pot.getPotName())
                        .potContent(pot.getPotContent())
                        .recruitmentDeadline(pot.getRecruitmentDeadline())
                        .dDay(ChronoUnit.DAYS.between(LocalDate.now(), pot.getRecruitmentDeadline()))
                        .build())
                .recruitmentDetails(pot.getRecruitmentDetails().stream()
                        .map(r -> PotResponseDto.RecruitmentDetailsDto.builder()
                                .recruitmentId(r.getRecruitmentId())
                                .recruitmentRole(r.getRecruitmentRole())
                                .recruitmentCount(r.getRecruitmentCount())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }


}