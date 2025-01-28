package stackpot.stackpot.converter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.web.dto.CompletedPotDetailResponseDto;

@Component
public class PotDetailConverterImpl implements PotDetailConverter {
    @Override
    public CompletedPotDetailResponseDto toCompletedPotDetailDto(Pot pot, String appealContent) {
        return CompletedPotDetailResponseDto.builder()
                .potId(pot.getPotId())
                .potName(pot.getPotName())
                .potStartDate(pot.getPotStartDate())
                .potEndDate(pot.getPotEndDate())
                .potContent(pot.getPotContent())
                .potStatus(pot.getPotStatus())
                .potSummary(pot.getPotSummary())
                .appealContent(appealContent)
                .build();
    }
}
