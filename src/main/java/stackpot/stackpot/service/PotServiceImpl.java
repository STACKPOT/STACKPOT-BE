package stackpot.stackpot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.repository.PotRepository.PotRepository;
import stackpot.stackpot.service.PotService;
import stackpot.stackpot.web.dto.PotResponseDTO;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PotServiceImpl implements PotService {

    private final PotRepository potRepository;

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
                                .potContent(pot.getPotContent())
                                .recruitmentDeadline(pot.getRecruitmentDeadline())
                                .dDay(ChronoUnit.DAYS.between(LocalDate.now(), pot.getRecruitmentDeadline()))
                                .build())
                        .recruitmentDetails(pot.getRecruitmentDetails().stream()
                                .map(r -> PotResponseDTO.RecruitmentDetailsDto.builder()
                                        .recruitmentId(r.getRecruitmentId())
                                        .recruitmentRole(r.getRecruitmentRole())
                                        .recruitmentCount(r.getRecruitmentCount())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public PotResponseDTO getPotDetails(Long potId) {
        Pot pot = potRepository.findPotWithRecruitmentDetailsByPotId(potId)
                .orElseThrow(() -> new IllegalArgumentException("Pot not found with id: " + potId));

        return PotResponseDTO.builder()
                .user(PotResponseDTO.UserDto.builder()
                        .nickname(pot.getUser().getNickname())
                        .role(pot.getUser().getRole())
                        .build())
                .pot(PotResponseDTO.PotDto.builder()
                        .potId(pot.getPotId())
                        .potName(pot.getPotName())
                        .potContent(pot.getPotContent())
                        .recruitmentDeadline(pot.getRecruitmentDeadline())
                        .dDay(ChronoUnit.DAYS.between(LocalDate.now(), pot.getRecruitmentDeadline()))
                        .build())
                .recruitmentDetails(pot.getRecruitmentDetails().stream()
                        .map(r -> PotResponseDTO.RecruitmentDetailsDto.builder()
                                .recruitmentId(r.getRecruitmentId())
                                .recruitmentRole(r.getRecruitmentRole())
                                .recruitmentCount(r.getRecruitmentCount())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}