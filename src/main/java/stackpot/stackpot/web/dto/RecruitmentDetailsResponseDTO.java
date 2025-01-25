package stackpot.stackpot.web.dto;


import lombok.Builder;
import lombok.Getter;
import stackpot.stackpot.domain.enums.Role;

@Getter
@Builder
public class RecruitmentDetailsResponseDTO {

    private Long recruitmentId;
    private Role recruitmentRole;
    private Integer recruitmentCount;

}
