package stackpot.stackpot.converter;

import stackpot.stackpot.domain.mapping.PotMemberBadge;
import stackpot.stackpot.web.dto.PotBadgeMemberDto;

public interface PotBadgeMemberConverter {
    PotBadgeMemberDto toDto(PotMemberBadge potMemberBadge);
}
