package stackpot.stackpot.pot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.EnumHandler;
import stackpot.stackpot.pot.dto.LikedApplicantResponseDTO;
import stackpot.stackpot.pot.dto.*;
import stackpot.stackpot.pot.service.potApplication.PotApplicationQueryService;
import stackpot.stackpot.pot.service.pot.PotCommandService;
import stackpot.stackpot.pot.service.pot.PotQueryService;
import stackpot.stackpot.user.entity.enums.Role;
import stackpot.stackpot.pot.repository.PotRepository;
import stackpot.stackpot.search.dto.CursorPageResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Pot  Management", description = "팟 관리 API")
@RestController
@RequestMapping(value ="/pots",produces = "application/json; charset=UTF-8")
@RequiredArgsConstructor
public class PotController {

    private final PotCommandService potCommandService;
    private final PotQueryService potQueryService;
    private final PotApplicationQueryService potApplicationQueryService;
    private final PotRepository potRepository;

    @Operation(summary = "팟 생성 API", description = """
        - potStatus: RECRUITING / ONGOING / COMPLETED
        - potStartDate, potEndDate: yyyy.MM 형식 (예: 2025.08)
        - potModeOfOperation: ONLINE / OFFLINE / HYBRID
        - Role: FRONTEND / BACKEND / DESIGN / PLANNING
    """)
    @PostMapping
    public ResponseEntity<ApiResponse<PotResponseDto>> createPot(@RequestBody @Valid PotRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.onSuccess(potCommandService.createPotWithRecruitments(requestDto)));
    }

    @Operation(summary = "팟 수정 API")
    @PatchMapping("/{pot_id}")
    public ResponseEntity<ApiResponse<PotResponseDto>> updatePot(@PathVariable("pot_id") Long potId, @RequestBody @Valid PotRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.onSuccess(potCommandService.updatePotWithRecruitments(potId, requestDto)));
    }

//    @Operation(summary = "팟 삭제 API")
//    @DeleteMapping("/{pot_id}")
//    public ResponseEntity<ApiResponse<Void>> deletePot(@PathVariable("pot_id") Long potId) {
//        potCommandService.deletePot(potId);
//        return ResponseEntity.ok(ApiResponse.onSuccess(null));
//    }

    @GetMapping("/completed")
    @Operation(summary = "내가 만든 팟 - 끓인 나의 팟 조회 API", description = "potStatus가 COMPLETED인 팟의 목록을 커서 기반 페이지네이션으로 가져옵니다.", parameters = {
            @Parameter(name = "cursor", description = "현재 페이지의 마지막 potId 값", example = "10"),
            @Parameter(name = "size", description = "한 페이지에 가져올 데이터 개수", example = "3")
    })
    public ResponseEntity<ApiResponse<CursorPageResponse<CompletedPotResponseDto>>> getMyCompletedPots(@RequestParam(value = "cursor", required = false) Long cursor, @RequestParam(value = "size", defaultValue = "3") int size) {
        return ResponseEntity.ok(ApiResponse.onSuccess(potQueryService.getMyCompletedPots(cursor, size)));
    }

    @GetMapping
    @Operation(
            summary = "모든 팟 조회 API",
            description = """
- recruitmentRoles: 여러 역할(FRONTEND, BACKEND 등)을 리스트로 받습니다.
  ex) /api/pots?recruitmentRoles=BACKEND&recruitmentRoles=FRONTEND
- onlyMine: true인 경우 로그인한 사용자가 만든 팟 중 모집 중(RECRUITING)인 팟만 조회합니다."""
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPots(
            @RequestParam(required = false) List<String> recruitmentRoles,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Boolean onlyMine) {

        if (page < 1) {
            throw new EnumHandler(ErrorStatus.INVALID_PAGE);
        }

        List<Role> roleEnums = null;
        if (recruitmentRoles != null && !recruitmentRoles.isEmpty()) {
            roleEnums = recruitmentRoles.stream()
                    .map(role -> Role.valueOf(role.trim().toUpperCase()))
                    .collect(Collectors.toList());
        }

        Map<String, Object> response = potQueryService.getAllPotsWithPaging(roleEnums, page, size, onlyMine);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(summary = "팟 상세 조회 API", description = "팟 작성 완료 후, <내가 만든 팟>에서 상세보기 페이지에서 팟 상세 정보를 조회하는 API입니다.")
    @GetMapping("/{pot_id}")
    public ResponseEntity<ApiResponse<PotDetailResponseDto>> getPotDetails(@PathVariable("pot_id") Long potId) {
        return ResponseEntity.ok(ApiResponse.onSuccess(potQueryService.getPotDetails(potId)));
    }

    @Operation(summary = "특정 팟 지원자의 '마음에 들어요' 상태 변경 API")
    @PatchMapping("/{pot_id}/applications/like")
    public ResponseEntity<ApiResponse<Void>> patchLikes(@PathVariable("pot_id") Long potId, @RequestBody LikeRequestDTO likeRequest) {
        potCommandService.patchLikes(potId, likeRequest.getApplicationId(), likeRequest.getLiked());
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

    @Operation(summary = "특정 팟의 '마음에 들어요' 지원자 조회 API")
    @GetMapping("/{pot_id}/applications/like")
    public ResponseEntity<ApiResponse<List<LikedApplicantResponseDTO>>> getLikedApplicants(@PathVariable("pot_id") Long potId) {
        return ResponseEntity.ok(ApiResponse.onSuccess(potQueryService.getLikedApplicants(potId)));
    }

    @Operation(summary = "내가 지원한 팟 조회 API")
    @GetMapping("/apply")
    public ResponseEntity<ApiResponse<List<AppliedPotResponseDto>>> getAppliedPots() {
        return ResponseEntity.ok(ApiResponse.onSuccess(potQueryService.getAppliedPots()));
    }

    @Operation(summary = "팟 구인글 AI 요약 API")
    @GetMapping("/{pot_id}/summary")
    public ResponseEntity<ApiResponse<PotSummaryResponseDTO>> getPotSummary(@PathVariable("pot_id") Long potId) {
        return ResponseEntity.ok(ApiResponse.onSuccess(potQueryService.getPotSummary(potId)));
    }

    @Operation(summary = "팟 다 끓이기 API")
    @PatchMapping("/{pot_id}/complete")
    public ResponseEntity<ApiResponse<PotResponseDto>> patchPot(@PathVariable("pot_id") Long potId, @RequestBody @Valid CompletedPotRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.onSuccess(potCommandService.patchPotWithRecruitments(potId, requestDto)));
    }

    @Operation(summary = "특정 Pot의 상세 정보 및 지원자 목록 조회 API")
    @GetMapping("/{pot_id}/details")
    public ResponseEntity<ApiResponse<PotDetailWithApplicantsResponseDto>> getPotDetailsAndApplicants(@PathVariable("pot_id") Long potId) {
        return ResponseEntity.ok(ApiResponse.onSuccess(potApplicationQueryService.getPotDetailsAndApplicants(potId)));
    }

    @Operation(summary = "내가 만든 팟 - 모집 중인 팟 조회 API")
    @GetMapping("/recruiting")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyRecruitingPots(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        if (page < 1) {
            throw new EnumHandler(ErrorStatus.INVALID_PAGE);
        }

        Map<String, Object> response = potQueryService.getMyRecruitingPotsWithPaging(page, size);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }


}
