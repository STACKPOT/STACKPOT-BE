package stackpot.stackpot.pot.service;

import stackpot.stackpot.pot.dto.PotMemberInfoResponseDto;

import java.util.List;

public interface PotMemberQueryService {
    List<PotMemberInfoResponseDto> getPotMembers(Long potId);
}
