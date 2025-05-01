package stackpot.stackpot.badge.service;

import stackpot.stackpot.badge.dto.PotBadgeMemberDto;

import java.util.List;

public interface PotBadgeMemberService {
    List<PotBadgeMemberDto> getBadgeMembersByPotId(Long potId);
}
