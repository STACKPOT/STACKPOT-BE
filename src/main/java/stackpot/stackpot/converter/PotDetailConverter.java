package stackpot.stackpot.converter;

import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.web.dto.AppliedPotResponseDto;
import stackpot.stackpot.web.dto.CompletedPotDetailResponseDto;
import stackpot.stackpot.web.dto.PotDetailResponseDto;

public interface PotDetailConverter {
    CompletedPotDetailResponseDto toCompletedPotDetailDto(Pot pot, String userPotRole, String appealContent);

    AppliedPotResponseDto toAppliedPotResponseDto(User user, Pot pot, String recruitmentDetails);

    PotDetailResponseDto toPotDetailResponseDto(User user, Pot pot, String recruitmentDetails, Boolean isOwner, Boolean isApplied);
}

