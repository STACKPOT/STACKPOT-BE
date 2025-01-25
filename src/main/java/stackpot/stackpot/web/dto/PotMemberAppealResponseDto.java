package stackpot.stackpot.web.dto;

import lombok.Builder;
import lombok.Getter;
import stackpot.stackpot.domain.enums.Role;

@Getter
@Builder
public class PotMemberAppealResponseDto {

        private Long potMemberId;
        private Long potId;
        private Long userId;
        private String roleName;
        private Boolean isOwner; // 팟 생성자인지 여부
        private String nickname; // 닉네임 + 역할
        private String appealContent;
}
