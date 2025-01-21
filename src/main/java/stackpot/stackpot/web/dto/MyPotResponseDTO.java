package stackpot.stackpot.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyPotResponseDTO {

    @JsonProperty("MyPots")
    private List<OngoingPotsDetail> ongoingPots;

    @Getter
    @Builder
    public static class OngoingPotsDetail {
        private UserResponseDto user;
        private PotResponseDto pot;
        private List<RecruitmentDetailsResponseDTO> recruitmentDetails;
        private List<PotMemberResponseDTO> potMembers;
    }
}
