package stackpot.stackpot.pot.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PotMemberAppealResponseDto {

        private Long potMemberId;
        private Long potId;
        private Long userId;
        private String roleName;
        private String kakaoId;
        private String nickname; // 닉네임 + 역할
        private String appealContent;
}
