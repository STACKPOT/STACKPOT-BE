package stackpot.stackpot.service;

import stackpot.stackpot.web.dto.PotRequestDto;
import stackpot.stackpot.web.dto.PotResponseDto;

public interface PotService {
    PotResponseDto createPotWithRecruitments(String token, PotRequestDto requestDto);
    PotResponseDto updatePotWithRecruitments(String token, Long potId, PotRequestDto requestDto);

    void deletePot(String token, Long potId);
}
