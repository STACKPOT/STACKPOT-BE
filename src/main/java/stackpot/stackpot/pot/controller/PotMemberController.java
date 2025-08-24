package stackpot.stackpot.pot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.pot.service.potMember.PotMemberCommandService;
import stackpot.stackpot.pot.dto.PotMemberAppealResponseDto;
import stackpot.stackpot.pot.dto.PotMemberInfoResponseDto;
import stackpot.stackpot.pot.dto.PotMemberRequestDto;
import stackpot.stackpot.pot.dto.UpdateAppealRequestDto;
import stackpot.stackpot.pot.service.potMember.PotMemberQueryService;

import java.util.List;

@RestController
@RequestMapping("/pots/{pot_id}/members")
@RequiredArgsConstructor
@Tag(name = "Pot Member Management", description = "팟 멤버 관리 API")
public class PotMemberController {

    private final PotMemberCommandService potMemberCommandService;
    private final PotMemberQueryService potMemberQueryService;

    @Operation(summary = "팟 멤버 정보 조회 API")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PotMemberInfoResponseDto>>> getPotMembers(
            @PathVariable("pot_id") Long potId) {
        List<PotMemberInfoResponseDto> response = potMemberQueryService.getPotMembers(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(
            summary = "팟 시작 API",
            description = "지원자 ID 리스트를 받아 팟 멤버를 추가합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<List<PotMemberAppealResponseDto>>> addPotMembers(
            @PathVariable("pot_id") Long potId,
            @RequestBody @Valid PotMemberRequestDto requestDto) {
        List<PotMemberAppealResponseDto> response = potMemberCommandService.addMembersToPot(potId, requestDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(summary = "여기서 저는요 작성 및 수정 API")
    @PatchMapping("/appeal")
    public ResponseEntity<ApiResponse<String>> updateAppealContent(
            @PathVariable("pot_id") Long potId,
            @RequestBody @Valid UpdateAppealRequestDto requestDto) {
        potMemberCommandService.updateAppealContent(potId, requestDto.getAppealContent());
        return ResponseEntity.ok(ApiResponse.onSuccess("어필 내용이 성공적으로 업데이트되었습니다."));
    }
}
