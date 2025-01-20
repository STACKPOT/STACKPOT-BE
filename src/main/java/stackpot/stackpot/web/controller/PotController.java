package stackpot.stackpot.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.config.security.JwtTokenProvider;
import stackpot.stackpot.service.PotService;
import stackpot.stackpot.web.dto.LikeRequestDTO;
import stackpot.stackpot.web.dto.LikedApplicantResponseDTO;
import stackpot.stackpot.web.dto.PotResponseDTO;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PotController {

    private final PotService potService;


    @GetMapping("/pots")
    public List<PotResponseDTO> getPots(@RequestParam(required = false) String recruitmentRole) {
        return potService.getAllPots(recruitmentRole);
    }

    @GetMapping("/pots/{pot_id}")
    public PotResponseDTO getPotDetails(@PathVariable("pot_id") Long potId) {
        return potService.getPotDetails(potId);
    }

    /*@PatchMapping("/pots/{pot_id}/applications/like")
    public ResponseEntity<Void> patchLikes(
            @PathVariable("pot_id") Long potId,
            @RequestBody LikeRequestDTO likeRequest) {
        potService.patchLikes(potId, likeRequest.getApplicationId(), likeRequest.getLiked());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pots/{pot_id}/applications/like")
    public ResponseEntity<List<LikedApplicantResponseDTO>> getLikedApplicants(@PathVariable("pot_id") Long potId) {
        List<LikedApplicantResponseDTO> likedApplicants = potService.getLikedApplicants(potId);
        return ResponseEntity.ok(likedApplicants);
    }

    @GetMapping("/pots/apply")
    public PotResponseDTO getAppliedPots() {
        return potService.getAppliedPots();
    }*/

    // 특정 팟 지원자의 좋아요 상태 변경
    @PatchMapping("/{pot_id}/applications/like")
    public ResponseEntity<Void> patchLikes(
            @PathVariable("pot_id") Long potId,
            @RequestBody LikeRequestDTO likeRequest) {
        potService.patchLikes(potId, likeRequest.getApplicationId(), likeRequest.getLiked());
        return ResponseEntity.ok().build();
    }

    // 특정 팟의 좋아요한 지원자 목록 조회
    @GetMapping("/{pot_id}/applications/like")
    public ResponseEntity<List<LikedApplicantResponseDTO>> getLikedApplicants(
            @PathVariable("pot_id") Long potId) {
        List<LikedApplicantResponseDTO> likedApplicants = potService.getLikedApplicants(potId);
        return ResponseEntity.ok(likedApplicants);
    }

    // 사용자가 지원한 팟 조회
    @GetMapping("/apply")
    public ResponseEntity<List<PotResponseDTO>> getAppliedPots() {
        return ResponseEntity.ok(potService.getAppliedPots());
    }

    // 사용자가 만든 팟 조회
    @GetMapping("/pots/my-pots")
    public ResponseEntity<List<PotResponseDTO>> getMyPots() {
        return ResponseEntity.ok(potService.getMyPots());
    }

}
