package stackpot.stackpot.converter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.web.dto.CompletedPotDetailResponseDto;

import java.time.format.DateTimeFormatter;

@Component
public class PotDetailConverterImpl implements PotDetailConverter {
    @Override
    public CompletedPotDetailResponseDto toCompletedPotDetailDto(Pot pot, String appealContent) {
        // 날짜 포맷 설정
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy. M. d");

        return CompletedPotDetailResponseDto.builder()
                .potId(pot.getPotId())
                .potName(pot.getPotName())
                .potStartDate(pot.getPotStartDate() != null ? pot.getPotStartDate().format(formatter) : null)
                .potEndDate(pot.getPotEndDate() != null ? pot.getPotEndDate().format(formatter) : null)
                .potContent(pot.getPotContent())
                .potStatus(pot.getPotStatus())
                .potSummary(pot.getPotSummary())
                .appealContent(appealContent)
                .build();
    }
}
