package stackpot.stackpot.pot.converter;

import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.pot.dto.AppliedPotResponseDto;
import stackpot.stackpot.pot.dto.CompletedPotDetailResponseDto;
import stackpot.stackpot.pot.dto.PotDetailResponseDto;

public interface PotDetailConverter {
    CompletedPotDetailResponseDto toCompletedPotDetailDto(Pot pot, String userPotRole, String appealContent);

    AppliedPotResponseDto toAppliedPotResponseDto(User user, Pot pot, String recruitmentDetails);

    PotDetailResponseDto toPotDetailResponseDto(User user, Pot pot, String recruitmentDetails, Boolean isOwner, Boolean isApplied);
}

