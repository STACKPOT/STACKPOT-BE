package stackpot.stackpot.service.PotMemberService;

import stackpot.stackpot.web.dto.PotMemberRequestDto;
import stackpot.stackpot.web.dto.PotMemberAppealResponseDto;

import java.util.List;

public interface PotMemberService {
    List<PotMemberAppealResponseDto> addMembersToPot(Long potId, PotMemberRequestDto requestDto);
    void updateAppealContent(Long potId, Long memberId, String appealContent);
}
