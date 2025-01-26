package stackpot.stackpot.converter;

import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.PotRecruitmentDetails;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.web.dto.CompletedPotResponseDto;
import stackpot.stackpot.web.dto.PotRequestDto;
import stackpot.stackpot.web.dto.PotResponseDto;
import stackpot.stackpot.web.dto.PotSearchResponseDto;

import java.util.List;
import java.util.Map;

public interface PotConverter {
    Pot toEntity(PotRequestDto dto,User user);
    CompletedPotResponseDto toCompletedPotResponseDto(Pot pot, Map<String, Integer> roleCounts);
    PotResponseDto toDto(Pot entity, List<PotRecruitmentDetails> recruitmentDetails);
    PotSearchResponseDto toSearchDto(Pot pot);
}
