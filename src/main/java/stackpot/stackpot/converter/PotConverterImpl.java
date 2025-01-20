package stackpot.stackpot.converter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.PotRecruitmentDetails;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.PotModeOfOperation;
import stackpot.stackpot.web.dto.PotRequestDto;
import stackpot.stackpot.web.dto.PotResponseDto;
import stackpot.stackpot.web.dto.PotRecruitmentResponseDto;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PotConverterImpl implements PotConverter {

    @Override
    public Pot toEntity(PotRequestDto dto, User user) {
        return Pot.builder()
                .user(user) // 사용자 정보 설정
                .potName(dto.getPotName())
                .potStartDate(dto.getPotStartDate())
                .potEndDate(dto.getPotEndDate())
                .potDuration(dto.getPotDuration())
                .potLan(dto.getPotLan())
                .potContent(dto.getPotContent())
                .potStatus(dto.getPotStatus())
                .potModeOfOperation(PotModeOfOperation.valueOf(dto.getPotModeOfOperation()))
                .potSummary(dto.getPotSummary())
                .recruitmentDeadline(dto.getRecruitmentDeadline())
                .build();
    }

    @Override
    public PotResponseDto toDto(Pot entity, List<PotRecruitmentDetails> recruitmentDetails) {
        return PotResponseDto.builder()
                .potId(entity.getPotId())
                .potName(entity.getPotName())
                .potStartDate(entity.getPotStartDate())
                .potEndDate(entity.getPotEndDate())
                .potDuration(entity.getPotDuration())
                .potLan(entity.getPotLan())
                .potContent(entity.getPotContent())
                .potStatus(entity.getPotStatus())
                .potModeOfOperation(entity.getPotModeOfOperation().name())
                .potSummary(entity.getPotSummary())
                .recruitmentDeadline(entity.getRecruitmentDeadline())
                .recruitmentDetails(recruitmentDetails.stream().map(r -> PotRecruitmentResponseDto.builder()
                        .recruitmentId(r.getRecruitmentId())
                        .recruitmentRole(r.getRecruitmentRole())
                        .recruitmentCount(r.getRecruitmentCount())
                        .build()).collect(Collectors.toList()))
                .build();
    }
}