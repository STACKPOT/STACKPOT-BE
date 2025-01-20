package stackpot.stackpot.service;

import stackpot.stackpot.web.dto.ApplicantResponseDTO;
import stackpot.stackpot.web.dto.LikedApplicantResponseDTO;
import stackpot.stackpot.web.dto.PotAllResponseDTO;

import java.util.List;

public interface PotService {
    // 모집 역할에 따라 모든 팟 조회
    List<PotAllResponseDTO.PotDetail> getAllPots(String role);

    // 특정 팟의 세부 정보 조회
    ApplicantResponseDTO getPotDetails(Long potId);

    // 특정 지원자의 좋아요 상태 수정
    void patchLikes(Long potId, Long applicationId, Boolean liked);

    // 특정 팟의 좋아요한 지원자 목록 조회
    List<LikedApplicantResponseDTO> getLikedApplicants(Long potId);

    // 사용자가 지원한 팟 목록 조회
    List<PotAllResponseDTO.PotDetail> getAppliedPots();

    // 사용자가 참여 중인 팟 목록 조회
    List<PotAllResponseDTO> getMyPots();

}