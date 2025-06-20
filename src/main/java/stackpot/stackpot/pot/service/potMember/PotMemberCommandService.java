package stackpot.stackpot.pot.service.potMember;

import stackpot.stackpot.pot.dto.PotMemberAppealResponseDto;
import stackpot.stackpot.pot.dto.PotMemberRequestDto;

import java.util.List;

public interface PotMemberCommandService {
    void removeMemberFromPot(Long potId);
    List<PotMemberAppealResponseDto> addMembersToPot(Long potId, PotMemberRequestDto requestDto);
    void updateAppealContent(Long potId, String appealContent);
}
