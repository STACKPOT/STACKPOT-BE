package stackpot.stackpot.service.PotMemberService;

import stackpot.stackpot.web.dto.PotMemberInfoResponseDto;
import stackpot.stackpot.web.dto.PotMemberRequestDto;
import stackpot.stackpot.web.dto.PotMemberAppealResponseDto;

import java.util.List;

public interface PotMemberService {
    List<PotMemberInfoResponseDto> getPotMembers(Long potId);
    List<PotMemberAppealResponseDto> addMembersToPot(Long potId, PotMemberRequestDto requestDto);
    void updateAppealContent(Long potId, String appealContent);
    void validateIsOwner(Long potId); // 팟 생성자 검증
}
