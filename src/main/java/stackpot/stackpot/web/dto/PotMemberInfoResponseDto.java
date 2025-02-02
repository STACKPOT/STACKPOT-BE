package stackpot.stackpot.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PotMemberInfoResponseDto {

    private String nickname; // 닉네임 + 역할
    private String kakaoId;
}
