package stackpot.stackpot.pot.service.potApplication;

import stackpot.stackpot.pot.dto.PotApplicationResponseDto;
import stackpot.stackpot.pot.dto.PotDetailWithApplicantsResponseDto;

import java.util.List;

public interface PotApplicationQueryService {

    List<PotApplicationResponseDto> getApplicantsByPotId(Long potId);

    PotDetailWithApplicantsResponseDto getPotDetailsAndApplicants(Long potId);
}
