package stackpot.stackpot.service;

import stackpot.stackpot.web.dto.PotBadgeMemberDto;

import java.util.List;

public interface PotBadgeMemberService {
    List<PotBadgeMemberDto> getBadgeMembersByPotId(Long potId);
}
