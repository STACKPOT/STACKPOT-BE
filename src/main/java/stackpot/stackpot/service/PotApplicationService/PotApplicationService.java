package stackpot.stackpot.service.PotApplicationService;

import stackpot.stackpot.web.dto.PotApplicationRequestDto;
import stackpot.stackpot.web.dto.PotApplicationResponseDto;
import stackpot.stackpot.web.dto.PotDetailWithApplicantsResponseDto;

import java.util.List;

public interface PotApplicationService {
    PotApplicationResponseDto applyToPot(PotApplicationRequestDto dto,Long potId);
    PotDetailWithApplicantsResponseDto getPotDetailsAndApplicants(Long potId);
    void cancelApplication(Long potId);
    List<PotApplicationResponseDto> getApplicantsByPotId(Long potId);
}

