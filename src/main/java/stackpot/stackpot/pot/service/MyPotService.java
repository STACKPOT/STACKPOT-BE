package stackpot.stackpot.pot.service;


import stackpot.stackpot.badge.dto.CompletedPotBadgeResponseDto;
import stackpot.stackpot.pot.dto.CompletedPotDetailResponseDto;
import stackpot.stackpot.pot.dto.OngoingPotResponseDto;

import java.util.List;

public interface MyPotService {

    // 사용자의 진행 중인 팟 조회
    List<OngoingPotResponseDto> getMyPots();
    CompletedPotDetailResponseDto getCompletedPotDetail(Long potId);
    List<CompletedPotBadgeResponseDto> getCompletedPotsWithBadges();
    List<CompletedPotBadgeResponseDto> getUserCompletedPotsWithBadges(Long userId);
    List<OngoingPotResponseDto> getMyOngoingPots();
    boolean isOwner(Long potId);


}