package stackpot.stackpot.converter;

import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.web.dto.CompletedPotDetailResponseDto;
import stackpot.stackpot.web.dto.PotDetailResponseDto;

public interface PotDetailConverter {
    CompletedPotDetailResponseDto toCompletedPotDetailDto(Pot pot, Role userPotRole, String appealContent);
    PotDetailResponseDto toPotDetailResponseDto(User user, Pot pot, String recruitmentDetails);
}

