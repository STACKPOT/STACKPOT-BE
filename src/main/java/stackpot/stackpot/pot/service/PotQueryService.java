package stackpot.stackpot.pot.service;

import stackpot.stackpot.pot.dto.*;
import stackpot.stackpot.search.dto.CursorPageResponse;
import stackpot.stackpot.user.entity.enums.Role;

import java.util.List;
import java.util.Map;

public interface PotQueryService {
    CursorPageResponse<CompletedPotResponseDto> getMyCompletedPots(Long cursor, int size);
    List<PotPreviewResponseDto> getAllPots(Role role, Integer page, Integer size);
    PotDetailResponseDto getPotDetails(Long potId);
    List<LikedApplicantResponseDTO> getLikedApplicants(Long potId);
    List<AppliedPotResponseDto> getAppliedPots();
    PotSummaryResponseDTO getPotSummary(Long potId);
    CursorPageResponse<CompletedPotResponseDto> getUserCompletedPots(Long userId, Long cursor, int size);
    CompletedPotDetailResponseDto getCompletedPotDetail(Long potId, Long userId);
    List<RecruitingPotResponseDto> getRecruitingPots();
    Map<String, Object> getAllPotsWithPaging(Role roleEnum, int page, int size);

}
