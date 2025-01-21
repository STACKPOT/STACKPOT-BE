package stackpot.stackpot.converter;

import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.PotRecruitmentDetails;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.web.dto.PotRequestDto;
import stackpot.stackpot.web.dto.PotResponseDto;

import java.util.List;

public interface PotConverter {
    Pot toEntity(PotRequestDto dto,User user);

    PotResponseDto toDto(Pot entity, List<PotRecruitmentDetails> recruitmentDetails);
}
