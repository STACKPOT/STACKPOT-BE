package stackpot.stackpot.converter;

import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.web.dto.CompletedPotDetailResponseDto;
import stackpot.stackpot.web.dto.PotDetailResponseDto;
import stackpot.stackpot.web.dto.RecruitmentDetailResponseDto;

import java.util.List;

public interface PotDetailConverter {
    CompletedPotDetailResponseDto toCompletedPotDetailDto(Pot pot, String appealContent);
    PotDetailResponseDto toPotDetailResponseDto(User user, Pot pot, String recruitmentDetails);
}

