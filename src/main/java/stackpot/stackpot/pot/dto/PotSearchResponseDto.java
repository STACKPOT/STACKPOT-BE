package stackpot.stackpot.pot.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PotSearchResponseDto {
    private Long potId;
    private String potName;
    private String potContent;
    private String creatorNickname;
    private String creatorRole;
    private String recruitmentPart;
    private LocalDate recruitmentDeadline;
}

