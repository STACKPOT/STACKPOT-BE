package stackpot.stackpot.pot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
public class PotMemberInfoResponseDto {
    private Long potMemberId;
    private String nickname; // 닉네임 + 역할
    private String potRole;
    private boolean owner;
}
