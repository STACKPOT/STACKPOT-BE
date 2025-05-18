package stackpot.stackpot.pot.converter;

import stackpot.stackpot.common.util.RoleNameMapper;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.pot.entity.mapping.PotApplication;
import stackpot.stackpot.pot.entity.mapping.PotMember;
import stackpot.stackpot.pot.dto.PotMemberAppealResponseDto;
import stackpot.stackpot.pot.dto.PotMemberInfoResponseDto;
import org.springframework.stereotype.Component;


import java.util.Map;

@Component
public class PotMemberConverter{

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

    public PotMemberAppealResponseDto toDto(PotMember entity) {
        String roleName = entity.getRoleName() != null ? entity.getRoleName().name() : "멤버";
        String nicknameWithRole = entity.getUser().getNickname() + " " + RoleNameMapper.mapRoleName(roleName);

        return PotMemberAppealResponseDto.builder()
                .potMemberId(entity.getPotMemberId())
                .potId(entity.getPot().getPotId())
                .userId(entity.getUser().getId())
                .roleName(roleName)
                .nickname(nicknameWithRole)
                .appealContent(entity.getAppealContent())
                .kakaoId(entity.getUser().getKakaoId())
                .build();
    }

    public PotMemberInfoResponseDto toKaKaoCreatorDto(PotMember entity) {
        String creatorRole = RoleNameMapper.mapRoleName(entity.getUser().getRole().name());
        String nicknameWithRole = entity.getUser().getNickname() + " " + creatorRole;

        return PotMemberInfoResponseDto.builder()
                .potMemberId(entity.getPotMemberId())
                .nickname(nicknameWithRole)
                .potRole(entity.getRoleName().name())
                .kakaoId(null)
                .owner(true)
                .build();
    }

    public PotMemberInfoResponseDto toKaKaoMemberDto(PotMember entity) {
        String roleName = entity.getPotApplication() != null
                ? entity.getPotApplication().getPotRole().name()
                : "멤버";
        String nicknameWithRole = entity.getUser().getNickname() + " " + RoleNameMapper.mapRoleName(roleName);

        return PotMemberInfoResponseDto.builder()
                .potMemberId(entity.getPotMemberId())
                .nickname(nicknameWithRole)
                .kakaoId(entity.getUser().getKakaoId())
                .owner(false)
                .potRole(roleName)
                .build();
    }
}