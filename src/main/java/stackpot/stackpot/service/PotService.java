package stackpot.stackpot.service;

import stackpot.stackpot.web.dto.PotRequestDto;
import stackpot.stackpot.web.dto.PotResponseDto;
import stackpot.stackpot.web.dto.*;

import java.util.List;


public interface PotService {
    PotResponseDto createPotWithRecruitments(String token, PotRequestDto requestDto);
    PotResponseDto updatePotWithRecruitments(String token, Long potId, PotRequestDto requestDto);

    void deletePot(String token, Long potId);

    //---------------

    // 모집 역할에 따라 모든 팟 조회
    List<PotAllResponseDTO.PotDetail>  getAllPots(String role, Integer page, Integer size);

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

    // 팟 다 끓이기
    void patchPotStatus(Long potId);

    PotSummaryResponseDTO getPotSummary(Long potId);
}
