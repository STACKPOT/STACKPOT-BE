package stackpot.stackpot.pot.converter;

import stackpot.stackpot.pot.dto.*;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.entity.PotRecruitmentDetails;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.entity.enums.Role;

import java.util.List;

public interface PotConverter {
    Pot toEntity(PotRequestDto dto, User user);
    CompletedPotResponseDto toCompletedPotResponseDto(Pot pot, String formattedMembers, Role userPotRole);
    PotResponseDto toDto(Pot entity, List<PotRecruitmentDetails> recruitmentDetails);
    PotSearchResponseDto toSearchDto(Pot pot);
    PotPreviewResponseDto toPrviewDto(User user, Pot pot, List<String> recruitmentRoles);
}
