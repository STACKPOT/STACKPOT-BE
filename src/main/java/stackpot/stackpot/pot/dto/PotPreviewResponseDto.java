package stackpot.stackpot.pot.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PotPreviewResponseDto {
    private Long userId;
    protected List<String> userRoles;
    private String userNickname;
    private Long potId;
    private String potName;
    private String potContent;
    private List<String> recruitmentRoles;
    private String dDay;
    private Boolean isSaved;
    private int potSaveCount;
    private Boolean isMember;
}
