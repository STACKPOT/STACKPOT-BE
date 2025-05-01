package stackpot.stackpot.pot.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PotApplicationResponseDto {
    private Long applicationId;
    private String potRole;
    private Long userId;
    private String userNickname;
}

