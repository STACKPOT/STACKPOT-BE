package stackpot.stackpot.web.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.repository.PotRepository.PotRepository;
import stackpot.stackpot.service.PotServiceImpl;
import stackpot.stackpot.web.dto.PotRequestDto;
import stackpot.stackpot.web.dto.PotResponseDto;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.service.PotService;
import stackpot.stackpot.web.dto.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Pot  Management", description = "팟 관리 API")
@RestController
@RequestMapping("/pots")
@RequiredArgsConstructor
public class PotController {


    private final PotService potService1;

    private final PotServiceImpl potService;
    private final PotRepository potRepository;

    @Operation(
            summary = "팟 생성하기",
            description = """
        - potStatus: RECRUITING / ONGOING / COMPLETED
        - potModeOfOperation: ONLINE / OFFLINE / HYBRID
        - Role: FRONTEND / BACKEND / DESIGN / PLANNING
    """
    )    @PostMapping
    public ResponseEntity<PotResponseDto> createPot(

            @Valid @RequestBody PotRequestDto requestDto) {
        // 팟 생성 로직 호출
        PotResponseDto responseDto = potService.createPotWithRecruitments(requestDto);

        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "팟 수정하기")
    @PatchMapping("/{pot_id}")
    public ResponseEntity<PotResponseDto> updatePot(
            @PathVariable("pot_id") Long potId,
            @RequestBody @Valid PotRequestDto requestDto) {
        // 팟 수정 로직 호출
        PotResponseDto responseDto = potService.updatePotWithRecruitments(potId, requestDto);

        return ResponseEntity.ok(responseDto); // 수정된 팟 정보 반환
    }

    @Operation(summary = "팟 삭제하기")
    @DeleteMapping("/{pot_id}")
    public ResponseEntity<Void> deletePot(@PathVariable("pot_id") Long potId) {
        // 팟 삭제 로직 호출
        potService.deletePot(potId);

        return ResponseEntity.noContent().build();
    }
    @GetMapping("/completed")
    @Operation(summary = "나의 끓인 팟 정보 가져오기", description = "potStatus가 COMPLETED인 팟의 목록을 가져옵니다.")
    public ResponseEntity<ApiResponse<List<CompletedPotResponseDto>>> getMyCompletedPots() {
        List<CompletedPotResponseDto> response = potService.getMyCompletedPots();
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
    //-------------------

    @Operation(
            summary = "팟 전체 보기 API",
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

        List<PotAllResponseDTO.PotDetail> pots = potService1.getAllPots(roleEnum, adjustedPage, size);

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
    @Operation(summary = "특정 팟의 상세정보 조회 API", description = "potId를 통해 특정 팟에 대한 상세정보를 조회할 수 있습니다. ")
    @GetMapping("/{pot_id}")
    public ResponseEntity<ApiResponse<ApplicantResponseDTO>> getPotDetails(@PathVariable("pot_id") Long potId) {
        ApplicantResponseDTO potDetails = potService1.getPotDetails(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(potDetails));
    }

    // 특정 팟 지원자의 좋아요 상태 변경
    @Operation(summary = "특정 팟 지원자의 '마음에 들어요' 상태 변경 API", description = "지원자의 아이디와 liked 값을 true, false (Boolean)로 request 해 주시면 지원자의 liked 상태값이 해당 값에 맞춰 변경됩니다.")
    @PatchMapping("/{pot_id}/applications/like")
    public ResponseEntity<ApiResponse<Void>> patchLikes(
            @PathVariable("pot_id") Long potId,
            @RequestBody LikeRequestDTO likeRequest) {
        potService1.patchLikes(potId, likeRequest.getApplicationId(), likeRequest.getLiked());
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

    // 특정 팟의 좋아요한 지원자 목록 조회
    @Operation(summary = "특정 팟의 '마음에 들어요' 지원자들 목록 조회 API", description = "지원자의 id, pot 지원 역할, 지원 역할에 따른 팟에서의 nickname, like 상태 값을 반환합니다. ")
    @GetMapping("/{pot_id}/applications/like")
    public ResponseEntity<ApiResponse<List<LikedApplicantResponseDTO>>> getLikedApplicants(
            @PathVariable("pot_id") Long potId) {
        List<LikedApplicantResponseDTO> likedApplicants = potService1.getLikedApplicants(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(likedApplicants));
    }

    // 사용자가 지원한 팟 조회
    @Operation(summary = "사용자가 지원한 팟 조회 API")
    @GetMapping("/apply")
    public ResponseEntity<ApiResponse<List<PotAllResponseDTO.PotDetail>>> getAppliedPots() {
        List<PotAllResponseDTO.PotDetail> appliedPots = potService1.getAppliedPots();
        return ResponseEntity.ok(ApiResponse.onSuccess(appliedPots));
    }

    // 사용자가 만든 팟 조회
    @Operation(summary = "사용자가 만든 팟 조회 API", description = "모집 중인 나의 팟, 진행 중인 나의 팟, 끓인 나의 팟을 구분하여 리스트 형식으로 전달합니다. 진행 중인 팟의 경우 멤버들의 사진이 보여야 하기에 potMembers 정보를 함께 전달합니다.")
    @GetMapping("/my-pots")
    public ResponseEntity<ApiResponse<List<PotAllResponseDTO>>> getMyPots() {
        List<PotAllResponseDTO> myPots = potService1.getMyPots();
        return ResponseEntity.ok(ApiResponse.onSuccess(myPots));
    }


    // Pot 내용 AI 요약
    @Operation(summary = "Pot 내용 AI 요약 API", description = "팟의 구인글 내용을 활용해 작성됩니다.")
    @GetMapping("/{pot_id}/summary")
    public ResponseEntity<ApiResponse<PotSummaryResponseDTO>> getPotSummary(@PathVariable("pot_id") Long potId) {
        PotSummaryResponseDTO summary = potService1.getPotSummary(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(summary));
    }

}