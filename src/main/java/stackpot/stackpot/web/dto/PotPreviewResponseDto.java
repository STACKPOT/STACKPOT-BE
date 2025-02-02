package stackpot.stackpot.web.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class PotPreviewResponseDto {
    private Long userId;
    protected String userRole;
    private String userNickname;
    private Long potId;
    private String potName;
    private String potContent;
    private String recruitmentRole;
    private String dDay;
}
