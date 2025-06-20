package stackpot.stackpot.pot.service.potMember;

import stackpot.stackpot.pot.dto.PotMemberInfoResponseDto;
import stackpot.stackpot.pot.dto.UserMemberIdDto;
import stackpot.stackpot.pot.entity.mapping.PotMember;
import stackpot.stackpot.user.entity.enums.Role;

import java.util.List;

public interface PotMemberQueryService {
    List<PotMemberInfoResponseDto> getPotMembers(Long potId);
    Long selectPotMemberIdByUserIdAndPotId(Long userId, Long potId);
    List<Long> selectPotMembersIdsByUserIdsAndPotId(List<Long> userIds, Long potId);
    List<Long> selectUserIdsAboutPotMembersByPotId(Long potId);
    List<UserMemberIdDto> selectPotMemberIdsByUserId(Long userId);
    List<PotMember> selectPotMembersByPotMemberIds(List<Long> potMemberIds);
    Role selectRoleByUserIdAndPotId(Long userId, Long potId);
}
