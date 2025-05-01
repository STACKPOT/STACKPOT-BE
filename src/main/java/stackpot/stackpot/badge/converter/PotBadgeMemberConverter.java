package stackpot.stackpot.badge.converter;

import stackpot.stackpot.badge.entity.mapping.PotMemberBadge;
import stackpot.stackpot.badge.dto.PotBadgeMemberDto;

public interface PotBadgeMemberConverter {
    PotBadgeMemberDto toDto(PotMemberBadge potMemberBadge);
}
