package stackpot.stackpot.badge.converter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.badge.dto.PotBadgeMemberDto;
import stackpot.stackpot.badge.entity.Badge;
import stackpot.stackpot.badge.entity.mapping.PotMemberBadge;
import stackpot.stackpot.pot.entity.mapping.PotMember;
import stackpot.stackpot.user.entity.User;


@Component
public class PotBadgeMemberConverter{

    public PotBadgeMemberDto toDto(PotMemberBadge potMemberBadge) {
        PotMember potMember = potMemberBadge.getPotMember();
        Badge badge = potMemberBadge.getBadge();
        User user = potMember.getUser();

        String roleName = mapRoleName(potMember.getRoleName().name());

        return PotBadgeMemberDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname() + " " + roleName )
                .badgeId(badge.getBadgeId())
                .badgeName(badge.getName())
                .build();
    }


    public String mapRoleName(String potRole) {
        switch (potRole) {
            case "BACKEND":
                return "양파";
            case "FRONTEND":
                return "버섯";
            case "DESIGN":
                return "브로콜리";
            case "PLANNING":
                return "당근";
            default:
                return "멤버";
        }
    }
}
