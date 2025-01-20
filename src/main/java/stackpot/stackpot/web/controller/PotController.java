package stackpot.stackpot.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.service.PotService;
import stackpot.stackpot.web.dto.ApplicantResponseDTO;
import stackpot.stackpot.web.dto.LikeRequestDTO;
import stackpot.stackpot.web.dto.LikedApplicantResponseDTO;
import stackpot.stackpot.web.dto.PotAllResponseDTO;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PotController {

    private final PotService potService;


    @GetMapping("/pots")
    public List<PotAllResponseDTO.PotDetail> getPots(@RequestParam(required = false) String recruitmentRole) {
        return potService.getAllPots(recruitmentRole);
    }

    @GetMapping("/pots/{pot_id}")
    public ApplicantResponseDTO getPotDetails(@PathVariable("pot_id") Long potId) {
        return potService.getPotDetails(potId);
    }

    // 특정 팟 지원자의 좋아요 상태 변경
    @PatchMapping("/pots/{pot_id}/applications/like")
    public ResponseEntity<Void> patchLikes(
            @PathVariable("pot_id") Long potId,
            @RequestBody LikeRequestDTO likeRequest) {
        potService.patchLikes(potId, likeRequest.getApplicationId(), likeRequest.getLiked());
        return ResponseEntity.ok().build();
    }

    // 특정 팟의 좋아요한 지원자 목록 조회
    @GetMapping("/pots/{pot_id}/applications/like")
    public ResponseEntity<List<LikedApplicantResponseDTO>> getLikedApplicants(
            @PathVariable("pot_id") Long potId) {
        List<LikedApplicantResponseDTO> likedApplicants = potService.getLikedApplicants(potId);
        return ResponseEntity.ok(likedApplicants);
    }

    // 사용자가 지원한 팟 조회
    @GetMapping("/pots/apply")
    public ResponseEntity<List<PotAllResponseDTO.PotDetail>> getAppliedPots() {
        return ResponseEntity.ok(potService.getAppliedPots());
    }

    // 사용자가 만든 팟 조회
    @GetMapping("/pots/my-pots")
    public ResponseEntity<List<PotAllResponseDTO>> getMyPots() {
        return ResponseEntity.ok(potService.getMyPots());
    }


}
