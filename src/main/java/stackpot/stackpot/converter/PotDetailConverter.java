package stackpot.stackpot.converter;

import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.web.dto.CompletedPotDetailResponseDto;

public interface PotDetailConverter {
    CompletedPotDetailResponseDto toCompletedPotDetailDto(Pot pot, String appealContent);
}

