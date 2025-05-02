package stackpot.stackpot.pot.service;

import stackpot.stackpot.pot.dto.PotMemberInfoResponseDto;
import stackpot.stackpot.pot.dto.PotMemberRequestDto;
import stackpot.stackpot.pot.dto.PotMemberAppealResponseDto;

import java.util.List;

public interface PotMemberService {
    List<PotMemberInfoResponseDto> getPotMembers(Long potId);
    List<PotMemberAppealResponseDto> addMembersToPot(Long potId, PotMemberRequestDto requestDto);
    void updateAppealContent(Long potId, String appealContent);
    void validateIsOwner(Long potId); // 팟 생성자 검증
}
