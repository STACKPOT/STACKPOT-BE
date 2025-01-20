package stackpot.stackpot.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
public class ApplicantResponseDTO {
    private UserResponseDTO user;
    private PotResponseDTO pot;
    private List<RecruitmentDetailsResponseDTO> recruitmentDetails;
    private List<ApplicantDto> applicant;

    @Getter
    @Builder
    public static class ApplicantDto {
        private Long applicationId;
        private String potRole;
        private Boolean liked;
    }
}
