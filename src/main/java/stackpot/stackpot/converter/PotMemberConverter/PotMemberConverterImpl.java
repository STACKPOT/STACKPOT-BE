package stackpot.stackpot.converter.PotMemberConverter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.mapping.PotApplication;
import stackpot.stackpot.domain.mapping.PotMember;
import stackpot.stackpot.web.dto.PotMemberRequestDto;
import stackpot.stackpot.web.dto.PotMemberResponseDto;

@Component
public class PotMemberConverterImpl implements PotMemberConverter {

    @Override
    public PotMember toEntity(User user, Pot pot, PotApplication application, Boolean isOwner) {
        return PotMember.builder()
                .user(user)
                .pot(pot)
                .potApplication(application)
                .roleName(application != null ? mapRoleName(application.getPotRole()) : "Owner")
                .isOwner(isOwner)
                .appealContent(null)
                .build();
    }

    @Override
    public PotMemberResponseDto toDto(PotMember entity) {
        return PotMemberResponseDto.builder()
                .potMemberId(entity.getPotMemberId())
                .potId(entity.getPot().getPotId())
                .userId(entity.getUser().getId())
                .roleName(entity.getRoleName())
                .isOwner(entity.getIsOwner())
                .appealContent(entity.getAppealContent())
                .build();
    }

    private String mapRoleName(String potRole) {
        switch (potRole) {
            case "백엔드":
                return "버섯";
            case "프론트엔드":
                return "당근";
            case "디자인":
                return "양파";
            case "기획":
                return "브로콜리";
            default:
                return "멤버";
        }
    }

}
