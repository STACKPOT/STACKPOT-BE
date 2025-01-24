package stackpot.stackpot.service.PotApplicationService;

import stackpot.stackpot.web.dto.PotApplicationRequestDto;
import stackpot.stackpot.web.dto.PotApplicationResponseDto;

import java.util.List;

public interface PotApplicationService {
    PotApplicationResponseDto applyToPot(PotApplicationRequestDto dto,Long potId);

    List<PotApplicationResponseDto> getApplicantsByPotId(Long potId);
}