package stackpot.stackpot.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.service.PotMemberService.PotMemberService;
import stackpot.stackpot.web.dto.PotMemberAppealResponseDto;
import stackpot.stackpot.web.dto.PotMemberInfoResponseDto;
import stackpot.stackpot.web.dto.PotMemberRequestDto;
import stackpot.stackpot.web.dto.UpdateAppealRequestDto;

import java.util.List;

@RestController
@RequestMapping("/pots/{pot_id}/members")
@RequiredArgsConstructor
@Tag(name = "Pot Member Management", description = "팟 멤버 관리 API")
public class PotMemberController {

    private final PotMemberService potMemberService;

    @Operation(summary = "팟 멤버 정보 (KAKAOID, 닉네임) 조회 API")
    @GetMapping // ✅ @PathVariable을 사용하려면 URL에 포함해야 함
    public ResponseEntity<ApiResponse<List<PotMemberInfoResponseDto>>> getPotMembers(
            @PathVariable("pot_id") Long potId) {

        //potMemberService.validateIsOwner(potId); // 팟 생성자 검증 추가
        List<PotMemberInfoResponseDto> response = potMemberService.getPotMembers(potId);

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
        List<PotMemberAppealResponseDto> response = potMemberService.addMembersToPot(potId, requestDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
    @Operation(summary = "팟 어필 API")
    @PatchMapping("/appeal")
    public ResponseEntity<ApiResponse<String>> updateAppealContent(
            @PathVariable("pot_id") Long potId,

            @RequestBody @Valid UpdateAppealRequestDto requestDto) {
        potMemberService.updateAppealContent(potId, requestDto.getAppealContent());
        return ResponseEntity.ok(ApiResponse.onSuccess("어필 내용이 성공적으로 업데이트되었습니다."));
    }
}
