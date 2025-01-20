package stackpot.stackpot.service;

import stackpot.stackpot.web.dto.LikedApplicantResponseDTO;
import stackpot.stackpot.web.dto.PotResponseDTO;

import java.util.List;

public interface PotService {
    List<PotResponseDTO> getAllPots(String role);
    PotResponseDTO getPotDetails(Long potId);
    void patchLikes(Long potId, Long applicationId, Boolean liked);
    List<LikedApplicantResponseDTO> getLikedApplicants(Long potId);
    List<PotResponseDTO> getAppliedPots();
    List<PotResponseDTO> getMyPots();
}