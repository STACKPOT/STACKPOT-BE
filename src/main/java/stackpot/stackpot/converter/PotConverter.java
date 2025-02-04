package stackpot.stackpot.converter;

import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.PotRecruitmentDetails;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.web.dto.*;

import java.util.List;

public interface PotConverter {
    Pot toEntity(PotRequestDto dto,User user);
    CompletedPotResponseDto toCompletedPotResponseDto(Pot pot, String formattedMembers, Role userPotRole);
    PotResponseDto toDto(Pot entity, List<PotRecruitmentDetails> recruitmentDetails);
    PotSearchResponseDto toSearchDto(Pot pot);
    PotPreviewResponseDto toPrviewDto(User user, Pot pot, List<String> recruitmentRoles);
}
