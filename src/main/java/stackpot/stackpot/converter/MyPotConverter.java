package stackpot.stackpot.converter;


import stackpot.stackpot.domain.Feed;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.web.dto.*;

import java.util.List;

public interface MyPotConverter {
    OngoingPotResponseDto convertToOngoingPotResponseDto(Pot pot);
    CompletedPotBadgeResponseDto toCompletedPotBadgeResponseDto(Pot pot, String formattedMembers, Role userPotRole, List<BadgeDto> myBadges);
}
