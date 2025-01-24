package stackpot.stackpot.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LikedApplicantResponseDTO {
    private Long applicationId;
    private String applicantRole;
    private String potNickname;  // user의 nickname + pot_role 조합
    private Boolean liked;
}
