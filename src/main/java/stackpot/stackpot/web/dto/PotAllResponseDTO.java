package stackpot.stackpot.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PotAllResponseDTO {
    @JsonProperty("recruitingPots")
    private List<PotDetail> recruitingPots;

    @JsonProperty("ongoingPots")
    private List<MyPotResponseDTO.OngoingPotsDetail> ongoingPots;

    @JsonProperty("completedPots")
    private List<CompletedPotResponseDto> completedPots;

    @Getter
    @Builder
    public static class PotDetail {
        private UserResponseDto.Userdto user;
        private PotResponseDto pot;
    }
}
