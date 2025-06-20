package stackpot.stackpot.pot.service.potApplication;

import stackpot.stackpot.pot.dto.PotApplicationRequestDto;
import stackpot.stackpot.pot.dto.PotApplicationResponseDto;

public interface PotApplicationCommandService {
    PotApplicationResponseDto applyToPot(PotApplicationRequestDto dto, Long potId);
    void cancelApplication(Long potId);
}

