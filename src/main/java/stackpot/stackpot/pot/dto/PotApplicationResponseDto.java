package stackpot.stackpot.pot.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PotApplicationResponseDto {
    private Long applicationId;
    private Map<String,String> potRole;
    private Long userId;
    private String userNickname;
}

