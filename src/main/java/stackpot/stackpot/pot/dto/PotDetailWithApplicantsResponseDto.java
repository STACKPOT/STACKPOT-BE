package stackpot.stackpot.pot.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
@Getter
@Builder
@JsonPropertyOrder
public class PotDetailWithApplicantsResponseDto {
    private PotDetailResponseDto potDetail;
    private List<PotApplicationResponseDto> applicants;
}
