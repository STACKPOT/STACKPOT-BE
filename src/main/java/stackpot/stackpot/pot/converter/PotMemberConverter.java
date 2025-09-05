package stackpot.stackpot.pot.converter;

import stackpot.stackpot.common.util.RoleNameMapper;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.pot.entity.mapping.PotApplication;
import stackpot.stackpot.pot.entity.mapping.PotMember;
import stackpot.stackpot.pot.dto.PotMemberAppealResponseDto;
import stackpot.stackpot.pot.dto.PotMemberInfoResponseDto;
import org.springframework.stereotype.Component;
import stackpot.stackpot.user.entity.enums.Role;

@Component
public class PotMemberConverter{

    public PotMember toEntity(User user, Pot pot, PotApplication application, Boolean isOwner) {
        return PotMember.builder()
                .user(user)
                .pot(pot)
                .potApplication(application)
                .roleName(
                        application != null
                                ? application.getPotRole()
                                : (user.getRoles().isEmpty() ? Role.UNKNOWN : user.getRoles().get(0))
                )
                .owner(isOwner)
                .appealContent(null)
                .build();
    }

    public PotMemberAppealResponseDto toDto(PotMember entity) {
        String roleName = entity.getRoleName() != null ? entity.getRoleName().name() : "멤버";
        String nicknameWithRole;

        if (entity.getUser() == null || entity.getUser().isDeleted()) {
            // 탈퇴한 사용자라면 roleName 제외
            nicknameWithRole = entity.getUser() != null ? entity.getUser().getNickname() : "(알 수 없음)";
        } else {
            // 정상 사용자
            nicknameWithRole = entity.getUser().getNickname() + " " + RoleNameMapper.mapRoleName(roleName);
        }


        return PotMemberAppealResponseDto.builder()
                .potMemberId(entity.getPotMemberId())
                .potId(entity.getPot().getPotId())
                .userId(entity.getUser().getId())
                .roleName(roleName)
                .nickname(nicknameWithRole)
                .appealContent(entity.getAppealContent())
                .build();
    }

    public PotMemberInfoResponseDto toKaKaoCreatorDto(PotMember entity) {
        String creatorRole = RoleNameMapper.mapRoleName(entity.getRoleName().name());
        String nicknameWithRole = entity.getUser().getNickname() + " " + creatorRole;

        return PotMemberInfoResponseDto.builder()
                .potMemberId(entity.getPotMemberId())
                .nickname(nicknameWithRole)
                .potRole(entity.getRoleName().name())
                .owner(entity.isOwner()) // true 고정 대신 실제 owner 여부 반영 추천
                .build();
    }

    public PotMemberInfoResponseDto toKaKaoMemberDto(PotMember entity) {
        String roleName = entity.getRoleName() != null
                ? entity.getRoleName().name()
                : "멤버";

        String nicknameWithRole;

        if (entity.getUser() == null || entity.getUser().isDeleted()) {
            // 탈퇴한 사용자라면 roleName 제외
            nicknameWithRole = entity.getUser() != null ? entity.getUser().getNickname() : "(알 수 없음)";
        } else {
            // 정상 사용자
            nicknameWithRole = entity.getUser().getNickname() + " " + RoleNameMapper.mapRoleName(roleName);
        }

        return PotMemberInfoResponseDto.builder()
                .potMemberId(entity.getPotMemberId())
                .nickname(nicknameWithRole)
                .owner(false)
                .potRole(roleName)
                .build();
    }
    public PotMember toCreatorEntity(User user, Pot pot, String potRole) {
        return PotMember.builder()
                .user(user)
                .pot(pot)
                .roleName(Role.fromString(potRole))
                .owner(true)
                .build();
    }
}