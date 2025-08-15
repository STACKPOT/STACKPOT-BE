package stackpot.stackpot.pot.service.pot;

import stackpot.stackpot.pot.dto.CompletedPotRequestDto;
import stackpot.stackpot.pot.dto.PotNameUpdateRequestDto;
import stackpot.stackpot.pot.dto.PotRequestDto;
import stackpot.stackpot.pot.dto.PotResponseDto;

public interface PotCommandService {

    PotResponseDto createPotWithRecruitments(PotRequestDto requestDto);

    PotResponseDto updatePotWithRecruitments(Long potId, PotRequestDto requestDto);

    void deletePot(Long potId);

    PotResponseDto patchPotWithRecruitments(Long potId, CompletedPotRequestDto requestDto);

    String removePotOrMember(Long potId);

    void patchLikes(Long potId, Long applicationId, Boolean liked);

    PotResponseDto updateCompletedPot(Long potId, CompletedPotRequestDto requestDto);

    String updatePotName(Long potId, PotNameUpdateRequestDto request);
}
