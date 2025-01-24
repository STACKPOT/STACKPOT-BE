package stackpot.stackpot.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PotMemberAppealResponseDto {

        private Long potMemberId;
        private Long potId;
        private Long userId;
        private String roleName;
        private Boolean owner;
        private String appealContent;
}
