package stackpot.stackpot.pot.service;

import stackpot.stackpot.pot.dto.PotApplicationRequestDto;
import stackpot.stackpot.pot.dto.PotApplicationResponseDto;
import stackpot.stackpot.pot.dto.PotDetailWithApplicantsResponseDto;

import java.util.List;

public interface PotApplicationService {
    PotApplicationResponseDto applyToPot(PotApplicationRequestDto dto,Long potId);
    PotDetailWithApplicantsResponseDto getPotDetailsAndApplicants(Long potId);
    void cancelApplication(Long potId);
    List<PotApplicationResponseDto> getApplicantsByPotId(Long potId);
}

