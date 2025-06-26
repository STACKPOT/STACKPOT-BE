package stackpot.stackpot.pot.service.potApplication;

import stackpot.stackpot.pot.dto.PotApplicationResponseDto;
import stackpot.stackpot.pot.dto.PotDetailWithApplicantsResponseDto;
import stackpot.stackpot.pot.entity.mapping.PotApplication;

import java.util.List;

public interface PotApplicationQueryService {

    List<PotApplicationResponseDto> getApplicantsByPotId(Long potId);

    PotDetailWithApplicantsResponseDto getPotDetailsAndApplicants(Long potId);

    PotApplication getPotApplicationById(Long applicationId);
}
