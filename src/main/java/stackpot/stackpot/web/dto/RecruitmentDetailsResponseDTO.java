package stackpot.stackpot.web.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecruitmentDetailsResponseDTO {

    private Long recruitmentId;
    private String recruitmentRole;
    private Integer recruitmentCount;

}
