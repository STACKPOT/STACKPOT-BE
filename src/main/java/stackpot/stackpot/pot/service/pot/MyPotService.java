package stackpot.stackpot.pot.service.pot;


import stackpot.stackpot.badge.dto.CompletedPotBadgeResponseDto;
import stackpot.stackpot.pot.dto.AppealContentDto;
import stackpot.stackpot.pot.dto.OngoingPotResponseDto;
import stackpot.stackpot.pot.dto.PotSummaryDto;

import java.util.List;

public interface MyPotService {

    // 사용자의 진행 중인 팟 조회
    List<OngoingPotResponseDto> getMyPots();
    AppealContentDto getAppealContent(Long potId);
    AppealContentDto getUserAppealContent(Long potId, Long targetUserId);
    PotSummaryDto getPotSummary(Long potId);
    List<CompletedPotBadgeResponseDto> getCompletedPotsWithBadges();
    List<CompletedPotBadgeResponseDto> getUserCompletedPotsWithBadges(Long userId);
    List<OngoingPotResponseDto> getMyOngoingPots();
    boolean isOwner(Long potId);
    List<OngoingPotResponseDto> getMyAllInvolvedPots(String dataType);
    List<OngoingPotResponseDto> getUserAllInvolvedPots(Long userId, String dataType);

    String patchDelegate(Long potId, Long memberId);
}