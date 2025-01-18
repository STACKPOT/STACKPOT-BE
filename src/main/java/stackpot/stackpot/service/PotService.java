// Service
package stackpot.stackpot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stackpot.stackpot.converter.PotConverter;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.PotRecruitmentDetails;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.repository.PotRecruitmentDetailsRepository;
import stackpot.stackpot.repository.PotRepository;
import stackpot.stackpot.web.dto.PotRequestDto;
import stackpot.stackpot.web.dto.PotResponseDto;
import stackpot.stackpot.web.dto.PotRecruitmentRequestDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PotService {

    private final PotRepository potRepository;
    private final PotRecruitmentDetailsRepository recruitmentDetailsRepository;
    private final PotConverter potConverter;

    @Transactional
    public PotResponseDto createPotWithRecruitments(PotRequestDto requestDto, User user) {
        // Create Pot entity
        Pot pot = potConverter.toEntity(requestDto, user);
        Pot savedPot = potRepository.save(pot);

        // Create and save recruitment details
        List<PotRecruitmentDetails> recruitmentDetails = requestDto.getRecruitmentDetails().stream()
                .map(recruitmentDto -> PotRecruitmentDetails.builder()
                        .recruitmentRole(recruitmentDto.getRecruitmentRole())
                        .recruitmentCount(recruitmentDto.getRecruitmentCount())
                        .pot(savedPot)
                        .build())
                .collect(Collectors.toList());

        recruitmentDetailsRepository.saveAll(recruitmentDetails);

        // Convert and return response DTO
        return potConverter.toDto(savedPot, recruitmentDetails);
    }
}
