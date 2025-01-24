package stackpot.stackpot.service.PotMemberService;

import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.web.dto.PotMemberRequestDto;
import stackpot.stackpot.web.dto.PotMemberResponseDto;

import java.util.List;

public interface PotMemberService {
    List<PotMemberResponseDto> addMembersToPot(Long potId, PotMemberRequestDto requestDto);
    void updateAppealContent(Long potId, Long memberId, String appealContent);
}
