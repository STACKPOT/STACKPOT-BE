package stackpot.stackpot.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.service.PotServiceImpl;
import stackpot.stackpot.web.dto.PotRequestDto;
import stackpot.stackpot.web.dto.PotResponseDto;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.service.PotService;
import stackpot.stackpot.web.dto.*;

import java.util.List;




@RestController
@RequestMapping("/pots")
@RequiredArgsConstructor
public class PotController {


    private final PotService potService1;

    private final PotServiceImpl potService;

    @PostMapping
    public ResponseEntity<PotResponseDto> createPot(
            @RequestHeader("Authorization") String token,
            @RequestBody @Valid PotRequestDto requestDto) {
        // Bearer 제거 후 토큰 전달
        String parsedToken = token.replace("Bearer ", "");
        PotResponseDto responseDto = potService.createPotWithRecruitments(parsedToken, requestDto);
        return ResponseEntity.ok(responseDto);
    }
    @PatchMapping("/{pot_id}")
    public ResponseEntity<PotResponseDto> updatePot(
            @RequestHeader("Authorization") String token,
            @PathVariable("pot_id") Long potId, // 동일하게 이름 매핑
            @RequestBody @Valid PotRequestDto requestDto) { // 요청 DTO 검증
        // Bearer 제거
        String parsedToken = token.replace("Bearer ", "");

        // 팟 수정 로직 호출
        PotResponseDto responseDto = potService.updatePotWithRecruitments(parsedToken, potId, requestDto);

        return ResponseEntity.ok(responseDto); // 수정된 팟 정보 반환
    }


    @DeleteMapping("/{pot_id}")
    public ResponseEntity<Void> deletePot(
            @RequestHeader("Authorization") String token,
            @PathVariable("pot_id") Long potId) { // 동일하게 이름 매핑
        // Bearer 제거
        String parsedToken = token.replace("Bearer ", "");

        // 팟 삭제 로직 호출
        potService.deletePot(parsedToken, potId);

        return ResponseEntity.noContent().build();
    }

    //----------------------------

    // 모든 팟 조회
    @GetMapping("/pots")
    public ResponseEntity<ApiResponse<List<PotAllResponseDTO.PotDetail>>> getPots(@RequestParam(required = false) String recruitmentRole) {
        List<PotAllResponseDTO.PotDetail> pots = potService1.getAllPots(recruitmentRole);
        return ResponseEntity.ok(ApiResponse.onSuccess(pots));
    }

    // 특정 팟의 상세정보 조회
    @GetMapping("/pots/{pot_id}")
    public ResponseEntity<ApiResponse<ApplicantResponseDTO>> getPotDetails(@PathVariable("pot_id") Long potId) {
        ApplicantResponseDTO potDetails = potService1.getPotDetails(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(potDetails));
    }

    // 특정 팟 지원자의 좋아요 상태 변경
    @PatchMapping("/pots/{pot_id}/applications/like")
    public ResponseEntity<ApiResponse<Void>> patchLikes(
            @PathVariable("pot_id") Long potId,
            @RequestBody LikeRequestDTO likeRequest) {
        potService1.patchLikes(potId, likeRequest.getApplicationId(), likeRequest.getLiked());
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

    // 특정 팟의 좋아요한 지원자 목록 조회
    @GetMapping("/pots/{pot_id}/applications/like")
    public ResponseEntity<ApiResponse<List<LikedApplicantResponseDTO>>> getLikedApplicants(
            @PathVariable("pot_id") Long potId) {
        List<LikedApplicantResponseDTO> likedApplicants = potService1.getLikedApplicants(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(likedApplicants));
    }

    // 사용자가 지원한 팟 조회
    @GetMapping("/pots/apply")
    public ResponseEntity<ApiResponse<List<PotAllResponseDTO.PotDetail>>> getAppliedPots() {
        List<PotAllResponseDTO.PotDetail> appliedPots = potService1.getAppliedPots();
        return ResponseEntity.ok(ApiResponse.onSuccess(appliedPots));
    }

    // 사용자가 만든 팟 조회
    @GetMapping("/pots/my-pots")
    public ResponseEntity<ApiResponse<List<PotAllResponseDTO>>> getMyPots() {
        List<PotAllResponseDTO> myPots = potService1.getMyPots();
        return ResponseEntity.ok(ApiResponse.onSuccess(myPots));
    }

    // 사용자가 만든 팟 다 끓이기
    @PatchMapping("/pots/{pot_id}/complete")
    public ResponseEntity<ApiResponse<Void>> patchPotStatus(@PathVariable("pot_id") Long potId) {
        potService1.patchPotStatus(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

    // Pot 내용 AI 요약
    @GetMapping("/pots/{pot_id}/summary")
    public ResponseEntity<ApiResponse<PotSummaryResponseDTO>> getPotSummary(@PathVariable("pot_id") Long potId) {
        PotSummaryResponseDTO summary = potService1.getPotSummary(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(summary));
    }

}