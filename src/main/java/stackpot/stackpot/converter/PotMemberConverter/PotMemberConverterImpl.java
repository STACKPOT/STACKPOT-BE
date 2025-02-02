package stackpot.stackpot.converter.PotMemberConverter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.mapping.PotApplication;
import stackpot.stackpot.domain.mapping.PotMember;
import stackpot.stackpot.web.dto.PotMemberAppealResponseDto;
import stackpot.stackpot.web.dto.PotMemberInfoResponseDto;

@Component
public class PotMemberConverterImpl implements PotMemberConverter {

    @Override
    public PotMember toEntity(User user, Pot pot, PotApplication application, Boolean isOwner) {
        return PotMember.builder()
                .user(user)
                .pot(pot)
                .potApplication(application)
                .roleName(application != null ? application.getPotRole() : user.getRole()) // PotRole Enum 그대로 사용
                .owner(isOwner)
                .appealContent(null)
                .build();
    }

    @Override
    public PotMemberAppealResponseDto toDto(PotMember entity) {
        String roleName = entity.getRoleName() != null ? entity.getRoleName().name() : "멤버";
        String nicknameWithRole = entity.getUser().getNickname() + " "+mapRoleName(roleName) ;

        return PotMemberAppealResponseDto.builder()
                .potMemberId(entity.getPotMemberId())
                .potId(entity.getPot().getPotId())
                .userId(entity.getUser().getId())
                .roleName(roleName)
                .nickname(nicknameWithRole)
//                .isOwner(entity.isOwner())
                .appealContent(entity.getAppealContent())
                .kakaoId(entity.getUser().getKakaoId())
                .build();
    }


    private String mapRoleName(String potRole) {
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