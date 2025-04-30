package stackpot.stackpot.converter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.mapping.PotApplication;
import stackpot.stackpot.domain.mapping.PotMember;
import stackpot.stackpot.web.dto.PotMemberAppealResponseDto;
import stackpot.stackpot.web.dto.PotMemberInfoResponseDto;

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
        String nicknameWithRole = entity.getUser().getNickname() + " "+mapRoleName(roleName) ;

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
        String creatorRole = mapRoleName(entity.getUser().getRole().name()); // 유저 테이블의 역할 사용
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
        String roleName = entity.getPotApplication() != null ? entity.getPotApplication().getPotRole().name() : "멤버";
        String nicknameWithRole = entity.getUser().getNickname() + " " + mapRoleName(roleName);

        return PotMemberInfoResponseDto.builder()
                .potMemberId(entity.getPotMemberId())
                .nickname(nicknameWithRole)
                .kakaoId(entity.getUser().getKakaoId())
                .owner(false)
                .potRole(roleName)
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


    private String getKoreanRoleName(String role) {
        Map<String, String> roleToKoreaneMap = Map.of(
                "BACKEND", "백엔드",
                "FRONTEND", "프론트엔드",
                "DESIGN", "디자인",
                "PLANNING", "기획"
        );
        return roleToKoreaneMap.getOrDefault(role, "알 수 없음");
    }

}