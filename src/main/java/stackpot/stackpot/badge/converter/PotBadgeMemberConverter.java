package stackpot.stackpot.badge.converter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.badge.dto.PotBadgeMemberDto;
import stackpot.stackpot.badge.entity.Badge;
import stackpot.stackpot.badge.entity.mapping.PotMemberBadge;
import stackpot.stackpot.common.util.RoleNameMapper;
import stackpot.stackpot.pot.entity.mapping.PotMember;
import stackpot.stackpot.user.entity.User;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class PotBadgeMemberConverter{

    public PotBadgeMemberDto toDto(PotMemberBadge potMemberBadge) {
        PotMember potMember = potMemberBadge.getPotMember();
        Badge badge = potMemberBadge.getBadge();
        User user = potMember.getUser();

        String roleName = RoleNameMapper.mapRoleName(potMember.getRoleName().name());

        return PotBadgeMemberDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname() + " " + roleName)
                .kakaoId(user.getKakaoId())
                .badgeId(badge.getBadgeId())
                .badgeName(badge.getName())
                .build();
    }
}
