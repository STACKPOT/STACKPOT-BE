package stackpot.stackpot.pot.service.pot;

import stackpot.stackpot.pot.dto.*;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.search.dto.CursorPageResponse;
import stackpot.stackpot.user.entity.enums.Role;

import java.util.List;
import java.util.Map;

public interface PotQueryService {
    CursorPageResponse<CompletedPotResponseDto> getMyCompletedPots(Long cursor, int size);
    PotDetailResponseDto getPotDetails(Long potId);
    List<LikedApplicantResponseDTO> getLikedApplicants(Long potId);
    List<AppliedPotResponseDto> getAppliedPots();
    PotSummaryResponseDTO getPotSummary(Long potId);
    CursorPageResponse<CompletedPotResponseDto> getUserCompletedPots(Long userId, Long cursor, int size);
    Map<String, Object> getAllPotsWithPaging(List<Role> roles, int page, int size, Boolean onlyMine);
    Map<String, Object> getMyRecruitingPotsWithPaging(Integer page, Integer size);
    Pot getPotByPotId(Long potId);
}
