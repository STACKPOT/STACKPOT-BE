package stackpot.stackpot.web.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecruitmentDetailResponseDto {
    private String recruitmentRole;
    private Integer recruitmentCount;
}

