package stackpot.stackpot.converter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.domain.Badge;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.mapping.PotMember;
import stackpot.stackpot.domain.mapping.PotMemberBadge;
import stackpot.stackpot.web.dto.PotBadgeMemberDto;

@Component
public class PotBadgeMemberConverterImpl implements PotBadgeMemberConverter {

    @Override
    public PotBadgeMemberDto toDto(PotMemberBadge potMemberBadge) {
        PotMember potMember = potMemberBadge.getPotMember();
        Badge badge = potMemberBadge.getBadge();
        User user = potMember.getUser();

        String roleName = mapRoleName(potMember.getRoleName().name());

        return PotBadgeMemberDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname() + " " + roleName )
                .kakaoId(user.getKakaoId())
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

