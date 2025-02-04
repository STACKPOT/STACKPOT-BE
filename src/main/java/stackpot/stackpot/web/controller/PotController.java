package stackpot.stackpot.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.EnumHandler;
import stackpot.stackpot.apiPayload.exception.handler.RecruitmentHandler;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.repository.PotRepository.PotRepository;
import stackpot.stackpot.service.MyPotService;
import stackpot.stackpot.service.PotApplicationService.PotApplicationService;
import stackpot.stackpot.service.PotService;
import stackpot.stackpot.service.PotServiceImpl;
import stackpot.stackpot.web.dto.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Pot  Management", description = "팟 관리 API")
@RestController
@RequestMapping(value ="/pots",produces = "application/json; charset=UTF-8")
@RequiredArgsConstructor
public class PotController {


    private final PotService potService1;
    private final MyPotService myPotService;
    private final PotApplicationService potApplicationService;

    private final PotServiceImpl potService;
    private final PotRepository potRepository;

    @Operation(
            summary = "팟 생성 API",
            description = """
    - potStatus: RECRUITING / ONGOING / COMPLETED
    - potModeOfOperation: ONLINE / OFFLINE / HYBRID
    - Role: FRONTEND / BACKEND / DESIGN / PLANNING
"""
    )
    @PostMapping
    public ResponseEntity<ApiResponse<PotResponseDto>> createPot(

            @RequestBody @Valid PotRequestDto requestDto) {
        // 팟 생성 로직 호출
        PotResponseDto responseDto = potService.createPotWithRecruitments(requestDto);

        return ResponseEntity.ok(ApiResponse.onSuccess(responseDto));
    }

    @Operation(summary = "팟 수정 API")
    @PatchMapping("/{pot_id}")
    public ResponseEntity<ApiResponse<PotResponseDto>> updatePot(
            @PathVariable("pot_id") Long potId,
            @RequestBody @Valid PotRequestDto requestDto) {
        // 팟 수정 로직 호출
        PotResponseDto responseDto = potService.updatePotWithRecruitments(potId, requestDto);

        return ResponseEntity.ok(ApiResponse.onSuccess(responseDto)); // 수정된 팟 정보 반환
    }

    @Operation(summary = "팟 삭제 API")
    @DeleteMapping("/{pot_id}")
    public ResponseEntity<ApiResponse<Void>> deletePot(@PathVariable("pot_id") Long potId) {
        // 팟 삭제 로직 호출
        potService.deletePot(potId);

        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }



    @GetMapping("/completed")
    @Operation(summary = "내가 만든 팟 - 끓인 나의 팟 조회 API", description = "potStatus가 COMPLETED인 팟의 목록을 커서 기반 페이지네이션으로 가져옵니다.",
            parameters = {
                    @Parameter(name = "cursor", description = "현재 페이지의 마지막 potId 값", example = "10"),
                    @Parameter(name = "size", description = "한 페이지에 가져올 데이터 개수", example = "3")
            })
    public ResponseEntity<ApiResponse<CursorPageResponse<CompletedPotResponseDto>>> getMyCompletedPots(
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "size", defaultValue = "3") int size) {
        CursorPageResponse<CompletedPotResponseDto> response = potService.getMyCompletedPots(cursor, size);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
    @Operation(summary = "특정 사용자의 끓인 팟 상세 조회")
    @GetMapping("/{pot_id}/users/{user_id}")
    public ResponseEntity<ApiResponse<CompletedPotDetailResponseDto>> getCompletedPotDetail(
            @PathVariable("pot_id") Long potId,
            @PathVariable("user_id") Long userId) {

        CompletedPotDetailResponseDto response = potService.getCompletedPotDetail(potId, userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    //-------------------

    @Operation(
            summary = "모든 팟 조회 API",
            description = """
        - Role: FRONTEND / BACKEND / DESIGN / PLANNING / (NULL)
        만약 null인 경우 모든 role에 대해서 조회합니다.    
    """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPots(
            @RequestParam(required = false) String recruitmentRole,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        if (page < 1) {
            throw new EnumHandler(ErrorStatus.INVALID_PAGE);
        }

        Role roleEnum = null;
        if (recruitmentRole != null && !recruitmentRole.isEmpty()) {
            try {
                roleEnum = Role.valueOf(recruitmentRole.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RecruitmentHandler(ErrorStatus.INVALID_ROLE);
            }
        }

        int adjustedPage = page - 1;

        List<PotPreviewResponseDto> pots = potService1.getAllPots(roleEnum, adjustedPage, size);

        Page<Pot> potPage = (roleEnum == null)
                ? potRepository.findAll(PageRequest.of(adjustedPage, size))
                : potRepository.findByRecruitmentDetails_RecruitmentRole(roleEnum, PageRequest.of(adjustedPage, size));

        Map<String, Object> response = new HashMap<>();
        response.put("pots", pots);
        response.put("totalPages", potPage.getTotalPages());
        response.put("currentPage", potPage.getNumber() + 1);
        response.put("totalElements", potPage.getTotalElements());

        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    // 특정 팟의 상세정보 조회
    @Operation(summary = "팟 상세 조회 API",
            description = "**'팟 작성 완료 후, <내가 만든 팟>에서 상세보기'** 페이지에서 팟 상세 정보를 조회하는 API입니다.\n\n" +
                    "- **모든 팟에서 특정 팟을 선택하면 해당 팟의 정보를 반환**합니다.\n" +
                    "- **`potId`를 통해 특정 팟의 상세 정보를 조회할 수 있습니다.**")
    @GetMapping("/{pot_id}")
    public ResponseEntity<ApiResponse<PotDetailResponseDto>> getPotDetails(@PathVariable("pot_id") Long potId) {
        PotDetailResponseDto potDetails = potService1.getPotDetails(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(potDetails));
    }

    // 특정 팟 지원자의 좋아요 상태 변경
    @Operation(summary = "특정 팟 지원자의 '마음에 들어요' 상태 변경 API",
            description = "**'<내가 만든 팟>에서 상세보기- 지원자 있음'** 페이지에서 지원자의 **'마음에 들어요' 상태를 변경하는 API**입니다.\n\n" +
                    "- **지원자의 ID와 `liked` 값을 `true` 또는 `false`(Boolean)로 요청**하면 해당 상태로 변경됩니다.")
    @PatchMapping("/{pot_id}/applications/like")
    public ResponseEntity<ApiResponse<Void>> patchLikes(
            @PathVariable("pot_id") Long potId,
            @RequestBody LikeRequestDTO likeRequest) {
        potService1.patchLikes(potId, likeRequest.getApplicationId(), likeRequest.getLiked());
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

    // 특정 팟의 좋아요한 지원자 목록 조회
    @Operation(summary = "특정 팟의 '마음에 들어요' 지원자 조회 API",
            description = "**'<내가 만든 팟>에서 상세보기- 지원자 있음'** 페이지에서 마음에 든 지원자 정보를 반환하는 API입니다.\n\n" +
                    "- **지원자의 ID, 팟 지원 역할, 해당 역할에 따른 닉네임, '마음에 들어요' 상태(like 값)를 포함**합니다.")
    @GetMapping("/{pot_id}/applications/like")
    public ResponseEntity<ApiResponse<List<LikedApplicantResponseDTO>>> getLikedApplicants(
            @PathVariable("pot_id") Long potId) {
        List<LikedApplicantResponseDTO> likedApplicants = potService1.getLikedApplicants(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(likedApplicants));
    }

    // 사용자가 지원한 팟 조회
    @Operation(summary = "내가 지원한 팟 조회 API", description = "'지원한 팟 조회'에 필요한 본인이 지원한 팟들 리스트를 리턴합니다.")
    @GetMapping("/apply")
    public ResponseEntity<ApiResponse<List<AppliedPotResponseDto>>> getAppliedPots() {
        List<AppliedPotResponseDto> appliedPots = potService1.getAppliedPots();
        return ResponseEntity.ok(ApiResponse.onSuccess(appliedPots));
    }


    // Pot 내용 AI 요약
    @Operation(summary = "팟 구인글 AI 요약 API",
            description = "**'팟을 다 끓였어요 클릭 시 작성 페이지'**에서 **AI 요약 생성 버튼**을 누르면 생성되는 `potSummary` 내용입니다.\n\n" +
                    "- **팟의 구인글 내용을 기반으로 자동 생성**됩니다.\n" +
                    "- **'팟 다 끓이기' 시 `potContent`(구인글)가 아닌 `potSummary`(소개글)에 저장해야 합니다.**")
    @GetMapping("/{pot_id}/summary")
    public ResponseEntity<ApiResponse<PotSummaryResponseDTO>> getPotSummary(@PathVariable("pot_id") Long potId) {
        PotSummaryResponseDTO summary = potService1.getPotSummary(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(summary));
    }

    @Operation(summary = "팟 다 끓이기 API ",
            description =
                    "**'소개 작성 후 완료 시 모달'**에 사용되는 팟 다 끓이기 API입니다.\npot의 status가 자동으로 COMPLETED로 바뀌고, 팟 멤버들의 온도가 5도 올라갑니다.\n" +
                    "- potStatus: COMPLETED\n" +
                    "- potModeOfOperation: ONLINE / OFFLINE / HYBRID\n" +
                    "- Role: FRONTEND / BACKEND / DESIGN / PLANNING")
    @PatchMapping("/{pot_id}/complete")
    public ResponseEntity<ApiResponse<PotResponseDto>> patchPot(
            @PathVariable("pot_id") Long potId,
            @RequestBody @Valid PotRequestDto requestDto) {
        // 팟 다 끓이기 로직 호출
        PotResponseDto responseDto = potService.patchPotWithRecruitments(potId, requestDto);

        return ResponseEntity.ok(ApiResponse.onSuccess(responseDto)); // 수정된 팟 정보 반환
    }

    @Operation(summary = "특정 Pot의 상세 정보 및 지원자 목록 조회 API", description = "모든 사용자가 Pot의 상세 정보를 조회할 수 있으며, 사용자가 `팟의 소유자`이고 상태가 `'RECRUITING'`이면 **지원자 목록**도 함께 반환됩니다.")
    @GetMapping("/{pot_id}/details")
    public ResponseEntity<ApiResponse<PotDetailWithApplicantsResponseDto>> getPotDetailsAndApplicants(
            @PathVariable("pot_id") Long potId) {

        PotDetailWithApplicantsResponseDto response = potApplicationService.getPotDetailsAndApplicants(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(summary = "내가 만든 팟 - 모집 중인 팟 조회 API", description = "내가 만든 팟의 모집 중인 팟을 조회합니다.")
    @GetMapping("/recruiting")
    public ResponseEntity<ApiResponse<List<RecruitingPotResponseDto>>> getRecruitingPots() {
        List<RecruitingPotResponseDto> response = potService.getRecruitingPots();
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    /*@Operation(summary = "내가 만든 팟 - 진행 중인 팟 조회 API", description = "내가 만든 팟의 진행 중인 팟을 조회합니다. members의 {FRONTEND : 1}는 팟 멤버들의 역할별 멤버수를 나타내므로 프로필 아이콘(버섯, 양파 등)을 나타내실 때 사용하시면 됩니다.")
    @GetMapping("/ongoing")
    public ResponseEntity<ApiResponse<List<OngoingPotResponseDto>>> getMyOngoingPots() {
        List<OngoingPotResponseDto> response = myPotService.getMyOngoingPots();
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }*/

    /*@GetMapping("/completed/{userId}")
    @Operation(summary = "사용자별 끓인 팟 조회 API",
            description = "'마이페이지 - 끓인 팟'에 필요한 사용자의 끓인 팟을 리턴합니다.\n사용자 ID로 potStatus가 COMPLETED인 팟 목록을 커서 기반 페이지네이션으로 가져옵니다.",
            parameters = {
                    @Parameter(name = "userId", description = "사용자 ID", example = "1"),
                    @Parameter(name = "cursor", description = "현재 페이지의 마지막 potId 값", example = "10"),
                    @Parameter(name = "size", description = "한 페이지에 가져올 데이터 개수", example = "3")
            })
    public ResponseEntity<ApiResponse<CursorPageResponse<CompletedPotResponseDto>>> getUserCompletedPots(
            @PathVariable Long userId,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "size", defaultValue = "3") int size) {
        CursorPageResponse<CompletedPotResponseDto> response = potService.getUserCompletedPots(userId, cursor, size);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }*/
}