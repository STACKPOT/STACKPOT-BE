package stackpot.stackpot.converter.PotApplicationConverter;

import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.mapping.PotApplication;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.web.dto.PotApplicationRequestDto;
import stackpot.stackpot.web.dto.PotApplicationResponseDto;

public interface PotApplicationConverter {
    PotApplication toEntity(PotApplicationRequestDto dto, Pot pot, User user);

    PotApplicationResponseDto toDto(PotApplication entity);
}