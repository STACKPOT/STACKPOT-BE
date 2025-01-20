package stackpot.stackpot.converter.PotApplicationConverter;

import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.mapping.PotApplication;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.web.dto.ApplicationRequestDto;
import stackpot.stackpot.web.dto.ApplicationResponseDto;

public interface PotApplicationConverter {
    PotApplication toEntity(ApplicationRequestDto dto, Pot pot, User user);

    ApplicationResponseDto toDto(PotApplication entity);
}