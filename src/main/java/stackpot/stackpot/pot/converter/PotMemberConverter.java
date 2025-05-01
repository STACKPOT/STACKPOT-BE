package stackpot.stackpot.pot.converter;

import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.pot.entity.mapping.PotApplication;
import stackpot.stackpot.pot.entity.mapping.PotMember;
import stackpot.stackpot.pot.dto.PotMemberAppealResponseDto;
import stackpot.stackpot.pot.dto.PotMemberInfoResponseDto;

public interface PotMemberConverter {
    PotMember toEntity(User user, Pot pot, PotApplication application, Boolean isOwner);
    PotMemberAppealResponseDto toDto(PotMember entity);
    PotMemberInfoResponseDto toKaKaoMemberDto(PotMember entity);
    PotMemberInfoResponseDto toKaKaoCreatorDto(PotMember entity);
}
