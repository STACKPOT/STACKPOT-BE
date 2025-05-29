package stackpot.stackpot.pot.service;

import stackpot.stackpot.pot.dto.PotMemberInfoResponseDto;
import stackpot.stackpot.pot.dto.UserMemberIdDto;

import java.util.List;

public interface PotMemberQueryService {
    List<PotMemberInfoResponseDto> getPotMembers(Long potId);
    Long selectPotMemberIdByUserIdAndPotId(Long userId, Long potId);
    List<Long> selectPotMembersIdsByUserIdsAndPotId(List<Long> userIds, Long potId);
    List<Long> selectUserIdsAboutPotMembersByPotId(Long potId);
    List<UserMemberIdDto> selectPotMemberIdsByUserId(Long userId);
}
