package stackpot.stackpot.web.dto;

import lombok.Builder;
import lombok.Getter;
import stackpot.stackpot.domain.enums.Role;

@Getter
@Builder
public class PotMemberResponseDTO {

        private Long potMemberId;
        private Role roleName;
        private Boolean owner;
        private String appealContent;
}
