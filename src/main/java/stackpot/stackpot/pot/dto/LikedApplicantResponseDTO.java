package stackpot.stackpot.pot.dto;

import lombok.Builder;
import lombok.Getter;
import stackpot.stackpot.user.entity.enums.Role;

@Getter
@Builder
public class LikedApplicantResponseDTO {
    private Long applicationId;
    private Role applicantRole;
    private String potNickname;  // user의 nickname + pot_role 조합
    private Boolean liked;
}
