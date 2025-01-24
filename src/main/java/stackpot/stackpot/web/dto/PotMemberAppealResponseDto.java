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
        private Role roleName;
        private Boolean owner;
        private String appealContent;
}
