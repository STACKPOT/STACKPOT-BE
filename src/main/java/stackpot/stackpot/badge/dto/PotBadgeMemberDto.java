package stackpot.stackpot.badge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PotBadgeMemberDto {
    private Long userId;
    private String nickname;
    private String kakaoId;
    private Long badgeId;
    private String badgeName;
}
