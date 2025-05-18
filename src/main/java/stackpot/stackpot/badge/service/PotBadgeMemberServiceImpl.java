package stackpot.stackpot.badge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stackpot.stackpot.badge.converter.PotBadgeMemberConverter;
import stackpot.stackpot.badge.entity.mapping.PotMemberBadge;
import stackpot.stackpot.badge.repository.PotMemberBadgeRepository;
import stackpot.stackpot.badge.dto.PotBadgeMemberDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PotBadgeMemberServiceImpl implements PotBadgeMemberService {

    private final PotMemberBadgeRepository potMemberBadgeRepository;
    private final PotBadgeMemberConverter potBadgeMemberConverter;

    @Override
    public List<PotBadgeMemberDto> getBadgeMembersByPotId(Long potId) {
        List<PotMemberBadge> potMemberBadges = potMemberBadgeRepository.findByPotMember_Pot_PotId(potId);
        return potMemberBadges.stream()
                .map(potBadgeMemberConverter::toDto)
                .collect(Collectors.toList());
    }
}
