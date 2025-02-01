package stackpot.stackpot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stackpot.stackpot.converter.PotBadgeMemberConverter;
import stackpot.stackpot.domain.mapping.PotMemberBadge;
import stackpot.stackpot.repository.BadgeRepository.PotMemberBadgeRepository;
import stackpot.stackpot.web.dto.PotBadgeMemberDto;

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
