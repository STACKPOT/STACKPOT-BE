package stackpot.stackpot.service.PotApplicationService;

import stackpot.stackpot.web.dto.ApplicationRequestDto;
import stackpot.stackpot.web.dto.ApplicationResponseDto;

import java.util.List;

public interface PotApplicationService {
    ApplicationResponseDto applyToPot(String token, Long potId, ApplicationRequestDto dto);

    List<ApplicationResponseDto> getApplicationsByPot(Long potId);
}