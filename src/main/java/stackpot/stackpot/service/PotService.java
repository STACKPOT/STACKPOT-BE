package stackpot.stackpot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
                                .potName(pot.getPotName())
                                .potContent(pot.getPotContent())
                                .recruitmentDeadline(pot.getRecruitmentDeadline())
                                .dDay(ChronoUnit.DAYS.between(LocalDate.now(), pot.getRecruitmentDeadline()))
                                .build())
                        .build())
                .collect(Collectors.toList());
    }


}