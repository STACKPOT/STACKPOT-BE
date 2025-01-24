package stackpot.stackpot.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PotMemberResponseDto {
    private Long potMemberId;
    private Long potId;
    private Long userId;
    private String roleName;
    private Boolean isOwner;
    private String appealContent;
}

