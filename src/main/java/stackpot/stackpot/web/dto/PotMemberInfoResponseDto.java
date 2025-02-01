package stackpot.stackpot.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PotMemberInfoResponseDto {
    private Long potMemberId;
    private Long potId;
    private Long userId;
    private String roleName;
    private String nickname; // 닉네임 + 역할
    private String kakaoId;
}
