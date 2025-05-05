package stackpot.stackpot.pot.service;

import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.user.entity.enums.Role;
import stackpot.stackpot.pot.dto.LikedApplicantResponseDTO;
import stackpot.stackpot.pot.dto.*;
import stackpot.stackpot.search.dto.CursorPageResponse;

import java.util.List;
import java.util.Optional;


public interface PotService {
    PotResponseDto createPotWithRecruitments(PotRequestDto requestDto);
    PotResponseDto updatePotWithRecruitments(Long potId, PotRequestDto requestDto);
    CursorPageResponse<CompletedPotResponseDto> getMyCompletedPots(Long cursor, int size);
    void deletePot(Long potId);
    void removeMemberFromPot(Long potId);
    String removePotOrMember(Long potId);

    //---------------

    // 모집 역할에 따라 모든 팟 조회
    List<PotPreviewResponseDto>  getAllPots(Role role, Integer page, Integer size);

    // 특정 팟의 세부 정보 조회
    PotDetailResponseDto getPotDetails(Long potId);

    // 특정 지원자의 좋아요 상태 수정
    void patchLikes(Long potId, Long applicationId, Boolean liked);

    // 특정 팟의 좋아요한 지원자 목록 조회
    List<LikedApplicantResponseDTO> getLikedApplicants(Long potId);

    // 사용자가 지원한 팟 목록 조회
    List<AppliedPotResponseDto> getAppliedPots();
    CompletedPotDetailResponseDto getCompletedPotDetail(Long potId, Long userId);

    PotSummaryResponseDTO getPotSummary(Long potId);

    CursorPageResponse<CompletedPotResponseDto> getUserCompletedPots(Long userId, Long cursor, int size);

    // 팟 다 끓이기
    PotResponseDto patchPotWithRecruitments(Long potId, CompletedPotRequestDto requestDto);

    List<RecruitingPotResponseDto> getRecruitingPots();

    PotResponseDto UpdateCompletedPot(Long potId, CompletedPotRequestDto requestDto);
}
