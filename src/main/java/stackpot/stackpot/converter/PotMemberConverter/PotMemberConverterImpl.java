package stackpot.stackpot.converter.PotMemberConverter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.domain.mapping.PotApplication;
import stackpot.stackpot.domain.mapping.PotMember;
import stackpot.stackpot.web.dto.PotMemberAppealResponseDto;

@Component
public class PotMemberConverterImpl implements PotMemberConverter {

    @Override
    public PotMember toEntity(User user, Pot pot, PotApplication application, Boolean isOwner) {
        return PotMember.builder()
                .user(user)
                .pot(pot)
                .potApplication(application)
                .roleName(application != null ? application.getPotRole() : null) // PotRole Enum 그대로 사용
                .owner(isOwner)
                .appealContent(null)
                .build();
    }

    @Override
    public PotMemberAppealResponseDto toDto(PotMember entity) {
        return PotMemberAppealResponseDto.builder()
                .potMemberId(entity.getPotMemberId())
                .potId(entity.getPot().getPotId())
                .userId(entity.getUser().getId())
                .roleName(entity.getRoleName()) // PotRole 그대로 반환
                .owner(entity.isOwner())
                .appealContent(entity.getAppealContent())
                .build();
    }




}
