package stackpot.stackpot.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.domain.Pot;
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


@RestController
@RequestMapping("/pots")
@RequiredArgsConstructor
public class PotController {


    private final PotService potService1;

    private final PotServiceImpl potService;

    private final PotRepository potRepository;

    @PostMapping
    public ResponseEntity<PotResponseDto> createPot(

            @RequestBody @Valid PotRequestDto requestDto) {
        // 팟 생성 로직 호출
        PotResponseDto responseDto = potService.createPotWithRecruitments(requestDto);

        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("/{pot_id}")
    public ResponseEntity<PotResponseDto> updatePot(
            @PathVariable("pot_id") Long potId,
            @RequestBody @Valid PotRequestDto requestDto) {
        // 팟 수정 로직 호출
        PotResponseDto responseDto = potService.updatePotWithRecruitments(potId, requestDto);

        return ResponseEntity.ok(responseDto); // 수정된 팟 정보 반환
    }


    @DeleteMapping("/{pot_id}")
    public ResponseEntity<Void> deletePot(@PathVariable("pot_id") Long potId) {
        // 팟 삭제 로직 호출
        potService.deletePot(potId);

        return ResponseEntity.noContent().build();
    }

    //----------------------------

    @Operation(summary = "팟 전체 보기 API", description = "Design, Backend, Frontend, PM으로 필터링 가능합니다. 만약 null인 경우 전체 카테고리에 대해서 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPots(
            @RequestParam(required = false) String recruitmentRole,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        List<PotAllResponseDTO.PotDetail> pots = potService1.getAllPots(recruitmentRole, page, size);

        Page<Pot> potPage = (recruitmentRole == null || recruitmentRole.isEmpty())
                ? potRepository.findAll(PageRequest.of(page, size))
                : potRepository.findByRecruitmentDetails_RecruitmentRole(recruitmentRole, PageRequest.of(page, size));

        Map<String, Object> response = new HashMap<>();
        response.put("pots", pots);
        response.put("totalPages", potPage.getTotalPages());
        response.put("currentPage", potPage.getNumber());
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
    @PatchMapping("/{pot_id}/applications/like")
    public ResponseEntity<ApiResponse<Void>> patchLikes(
            @PathVariable("pot_id") Long potId,
            @RequestBody LikeRequestDTO likeRequest) {
        potService1.patchLikes(potId, likeRequest.getApplicationId(), likeRequest.getLiked());
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

    // 특정 팟의 좋아요한 지원자 목록 조회
    @GetMapping("/{pot_id}/applications/like")
    public ResponseEntity<ApiResponse<List<LikedApplicantResponseDTO>>> getLikedApplicants(
            @PathVariable("pot_id") Long potId) {
        List<LikedApplicantResponseDTO> likedApplicants = potService1.getLikedApplicants(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(likedApplicants));
    }

    // 사용자가 지원한 팟 조회
    @GetMapping("/apply")
    public ResponseEntity<ApiResponse<List<PotAllResponseDTO.PotDetail>>> getAppliedPots() {
        List<PotAllResponseDTO.PotDetail> appliedPots = potService1.getAppliedPots();
        return ResponseEntity.ok(ApiResponse.onSuccess(appliedPots));
    }

    // 사용자가 만든 팟 조회
    @GetMapping("/my-pots")
    public ResponseEntity<ApiResponse<List<PotAllResponseDTO>>> getMyPots() {
        List<PotAllResponseDTO> myPots = potService1.getMyPots();
        return ResponseEntity.ok(ApiResponse.onSuccess(myPots));
    }

    // 사용자가 만든 팟 다 끓이기
    @PatchMapping("/{pot_id}/complete")
    public ResponseEntity<ApiResponse<Void>> patchPotStatus(@PathVariable("pot_id") Long potId) {
        potService1.patchPotStatus(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

    // Pot 내용 AI 요약
    @GetMapping("/{pot_id}/summary")
    public ResponseEntity<ApiResponse<PotSummaryResponseDTO>> getPotSummary(@PathVariable("pot_id") Long potId) {
        PotSummaryResponseDTO summary = potService1.getPotSummary(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(summary));
    }

}