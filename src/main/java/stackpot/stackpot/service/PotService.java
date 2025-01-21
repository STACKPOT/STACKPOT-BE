package stackpot.stackpot.service;

import stackpot.stackpot.web.dto.PotRequestDto;
import stackpot.stackpot.web.dto.PotResponseDto;

public interface PotService {
    PotResponseDto createPotWithRecruitments(PotRequestDto requestDto);
    PotResponseDto updatePotWithRecruitments(Long potId, PotRequestDto requestDto);

    void deletePot(Long potId);
}
