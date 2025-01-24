package stackpot.stackpot.converter.PotMemberConverter;

import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.mapping.PotApplication;
import stackpot.stackpot.domain.mapping.PotMember;
import stackpot.stackpot.web.dto.PotMemberAppealResponseDto;

public interface PotMemberConverter {
    PotMember toEntity(User user, Pot pot, PotApplication application, Boolean isOwner);
    PotMemberAppealResponseDto toDto(PotMember entity);
}
