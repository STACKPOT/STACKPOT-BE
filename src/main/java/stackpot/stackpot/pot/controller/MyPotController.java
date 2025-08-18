package stackpot.stackpot.pot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.badge.dto.CompletedPotBadgeResponseDto;
import stackpot.stackpot.pot.dto.AppealContentDto;
import stackpot.stackpot.pot.dto.OngoingPotResponseDto;
import stackpot.stackpot.pot.dto.PotNameUpdateRequestDto;
import stackpot.stackpot.pot.service.pot.MyPotService;
import stackpot.stackpot.pot.service.pot.PotCommandService;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "My Pot Management", description = "나의 팟 관리 API")
@RequestMapping(value = "/my-pots",produces = "application/json; charset=UTF-8")
public class MyPotController {

    private final MyPotService myPotService;
    private final PotCommandService potCommandService;

    // 사용자가 만든 진행 중인 팟 조회
    @Operation(summary = "나의 팟 조회 API",
            description = "'나의 팟 첫 페이지' 정보를 반환하는 API입니다.\n\n" +
                    "- **사용자가 생성했거나 참여 중인 진행 중(`ONGOING`)인 팟을 조회**합니다.\n" +
                    "- **`isOwner` 값을 통해 사용자가 팟의 생성자인지 확인할 수 있습니다.**\n" +
                    "- **팟 생성일 기준 최신순으로 정렬**하여 반환됩니다.")
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<OngoingPotResponseDto>>> getMyPots() {
        List<OngoingPotResponseDto> response = myPotService.getMyPots();
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @DeleteMapping("/{pot_id}/members")
    @Operation(summary = "팟 멤버 또는 팟 삭제 API", description = "생성자는 팟을 삭제하며, 생성자가 아니면 팟 멤버에서 본인을 삭제합니다.")
    public ResponseEntity<ApiResponse<String>> removePotOrMember(
            @PathVariable("pot_id") Long potId) {
        String responseMessage = potCommandService.removePotOrMember(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(responseMessage));
    }

    @GetMapping("/{pot_id}/details")
    @Operation(summary = "마이페이지 끓인 팟 상세 보기 모달", description = "'끓인 팟 상세보기 모달'에 쓰이는 COMPLETED 상태인 팟의 상세 정보를 가져옵니다. 팟 멤버들의 userPotRole : num과 나의 역할도 함께 반환합니다.")
    public ResponseEntity<ApiResponse<AppealContentDto>> getCompletedPotDetail(
            @PathVariable("pot_id") Long potId) {
        AppealContentDto response = myPotService.getAppealContent(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @GetMapping("/badges")
    @Operation(summary = "나의 끓인 팟 조회 API (뱃지) - 마이페이지", description = "사용자가 참여한 potStatus가 COMPLETED 상태의 팟을 뱃지와 함께 반환합니다.")
    public ResponseEntity<ApiResponse<List<CompletedPotBadgeResponseDto>>> getCompletedPotsWithBadges() {
        List<CompletedPotBadgeResponseDto> response = myPotService.getCompletedPotsWithBadges();
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @GetMapping("/{user_id}/badges")
    @Operation(summary = "사용자별 끓인 팟 조회 API (뱃지) - 마이페이지", description = "userId를 통해 사용자별 참여한 potStatus가 COMPLETED 상태의 팟을 뱃지와 함께 반환합니다.")
    public ResponseEntity<ApiResponse<List<CompletedPotBadgeResponseDto>>> getUserCompletedPotsWithBadges(
            @PathVariable("user_id") Long userId
    ) {
        List<CompletedPotBadgeResponseDto> response = myPotService.getUserCompletedPotsWithBadges(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @GetMapping("/{pot_id}/isOwner")
    @Operation(summary = "팟 소유자 확인 API", description = "potId를 통해 해당 팟의 소유자인지 아닌지 체크합니다.")
    public ResponseEntity<ApiResponse<Boolean>> checkPotOwner(@PathVariable("pot_id") Long potId) {
        boolean isOwner = myPotService.isOwner(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(isOwner));
    }


    @PatchMapping("/{pot_id}/delegate/{member_id}")
    @Operation(summary = "팀장 권한 위임 API", description = "본인이 팀장인 경우, 팀장 권한을 특정 팀원에게 위임하는 기능입니다.")
    public ResponseEntity<ApiResponse<String>> patchDelegate(@PathVariable("pot_id") Long potId, @PathVariable("member_id") Long memberId) {
        String responseMessage = myPotService.patchDelegate(potId, memberId);
        return ResponseEntity.ok(ApiResponse.onSuccess(responseMessage));
    }

    @Operation(summary = "팟 이름 수정 API")
    @PatchMapping("/{pot_id}/rename")
    public ResponseEntity<ApiResponse<String>> updatePotName(
            @PathVariable Long pot_id,
            @Valid @RequestBody PotNameUpdateRequestDto request
    ) {
        String res = potCommandService.updatePotName(pot_id, request);
        return ResponseEntity.ok(ApiResponse.onSuccess(res));
    }

}
