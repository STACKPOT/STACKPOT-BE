package stackpot.stackpot.web.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PotApplicationResponseDto {
    private Long applicationId;
    private String potRole;
//    private Boolean liked;
    private String status;
//    private LocalDateTime appliedAt;
//    private Long potId;
    private Long userId;
    private String userNickname;
}

