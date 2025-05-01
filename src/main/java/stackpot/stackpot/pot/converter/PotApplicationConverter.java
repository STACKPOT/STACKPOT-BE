package stackpot.stackpot.pot.converter;

import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.entity.mapping.PotApplication;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.pot.dto.PotApplicationRequestDto;
import stackpot.stackpot.pot.dto.PotApplicationResponseDto;

public interface PotApplicationConverter {
    PotApplication toEntity(PotApplicationRequestDto dto, Pot pot, User user);

    PotApplicationResponseDto toDto(PotApplication entity);
}