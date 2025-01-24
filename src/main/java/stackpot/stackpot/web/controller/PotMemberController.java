package stackpot.stackpot.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.service.PotMemberService.PotMemberService;
import stackpot.stackpot.web.dto.PotMemberRequestDto;
import stackpot.stackpot.web.dto.PotMemberResponseDto;
import stackpot.stackpot.web.dto.UpdateAppealRequestDto;

import java.util.List;

@RestController
@RequestMapping("/pots/{pot_id}/members")
@RequiredArgsConstructor
public class PotMemberController {

    private final PotMemberService potMemberService;

    @Operation(summary = "팟 시작하기")
    @PostMapping
    public ResponseEntity<ApiResponse<List<PotMemberResponseDto>>> addPotMembers(
            @PathVariable("pot_id") Long potId,
            @RequestBody PotMemberRequestDto requestDto) {
        List<PotMemberResponseDto> response = potMemberService.addMembersToPot(potId, requestDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
    @Operation(summary = "팟 어필하기")
    @PatchMapping("/{member_id}/appeal")
    public ResponseEntity<ApiResponse<String>> updateAppealContent(
            @PathVariable("pot_id") Long potId,
            @PathVariable("member_id") Long memberId,
            @RequestBody @Valid UpdateAppealRequestDto requestDto) {
        potMemberService.updateAppealContent(potId, memberId, requestDto.getAppealContent());
        return ResponseEntity.ok(ApiResponse.onSuccess("어필 내용이 성공적으로 업데이트되었습니다."));
    }
}
