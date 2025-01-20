package stackpot.stackpot.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.service.PotService;
import stackpot.stackpot.web.dto.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PotController {

    private final PotService potService;

    // 모든 팟 조회
    @GetMapping("/pots")
    public ResponseEntity<ApiResponse<List<PotAllResponseDTO.PotDetail>>> getPots(@RequestParam(required = false) String recruitmentRole) {
        List<PotAllResponseDTO.PotDetail> pots = potService.getAllPots(recruitmentRole);
        return ResponseEntity.ok(ApiResponse.onSuccess(pots));
    }

    // 특정 팟의 상세정보 조회
    @GetMapping("/pots/{pot_id}")
    public ResponseEntity<ApiResponse<ApplicantResponseDTO>> getPotDetails(@PathVariable("pot_id") Long potId) {
        ApplicantResponseDTO potDetails = potService.getPotDetails(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(potDetails));
    }

    // 특정 팟 지원자의 좋아요 상태 변경
    @PatchMapping("/pots/{pot_id}/applications/like")
    public ResponseEntity<ApiResponse<Void>> patchLikes(
            @PathVariable("pot_id") Long potId,
            @RequestBody LikeRequestDTO likeRequest) {
        potService.patchLikes(potId, likeRequest.getApplicationId(), likeRequest.getLiked());
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

    // 특정 팟의 좋아요한 지원자 목록 조회
    @GetMapping("/pots/{pot_id}/applications/like")
    public ResponseEntity<ApiResponse<List<LikedApplicantResponseDTO>>> getLikedApplicants(
            @PathVariable("pot_id") Long potId) {
        List<LikedApplicantResponseDTO> likedApplicants = potService.getLikedApplicants(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(likedApplicants));
    }

    // 사용자가 지원한 팟 조회
    @GetMapping("/pots/apply")
    public ResponseEntity<ApiResponse<List<PotAllResponseDTO.PotDetail>>> getAppliedPots() {
        List<PotAllResponseDTO.PotDetail> appliedPots = potService.getAppliedPots();
        return ResponseEntity.ok(ApiResponse.onSuccess(appliedPots));
    }

    // 사용자가 만든 팟 조회
    @GetMapping("/pots/my-pots")
    public ResponseEntity<ApiResponse<List<PotAllResponseDTO>>> getMyPots() {
        List<PotAllResponseDTO> myPots = potService.getMyPots();
        return ResponseEntity.ok(ApiResponse.onSuccess(myPots));
    }

    // 사용자가 만든 팟 다 끓이기
    @PatchMapping("/pots/{pot_id}/complete")
    public ResponseEntity<ApiResponse<Void>> patchPotStatus(@PathVariable("pot_id") Long potId) {
        potService.patchPotStatus(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

    // Pot 내용 AI 요약
    @GetMapping("/pots/{pot_id}/summary")
    public ResponseEntity<ApiResponse<PotSummaryResponseDTO>> getPotSummary(@PathVariable("pot_id") Long potId) {
        PotSummaryResponseDTO summary = potService.getPotSummary(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(summary));
    }
}