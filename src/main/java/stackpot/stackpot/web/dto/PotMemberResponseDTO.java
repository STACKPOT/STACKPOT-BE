package stackpot.stackpot.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PotMemberResponseDTO {

        private Long potMemberId;
        private String roleName;
        private Boolean owner;
        private String appealContent;
}
