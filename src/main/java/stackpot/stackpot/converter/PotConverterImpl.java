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

import java.time.format.DateTimeFormatter;

@Component
public class PotConverterImpl implements PotConverter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    @Override
    public Pot toEntity(PotRequestDto requestDto, User user) {
        return Pot.builder()
                .potName(requestDto.getPotName())
                .potStartDate(requestDto.getPotStartDate())
                .potEndDate(requestDto.getPotEndDate())
                .potDuration(requestDto.getPotDuration())
                .potLan(requestDto.getPotLan())
                .potContent(requestDto.getPotContent())
                .potStatus(requestDto.getPotStatus())
                .potModeOfOperation(PotModeOfOperation.valueOf(requestDto.getPotModeOfOperation()))
                .potSummary(requestDto.getPotSummary())
                .recruitmentDeadline(requestDto.getRecruitmentDeadline())
                .user(user) // 사용자 설정
                .build();
    }

    @Override
    public PotResponseDto toDto(Pot entity, List<PotRecruitmentDetails> recruitmentDetails) {
        return PotResponseDto.builder()
                .potId(entity.getPotId())
                .potName(entity.getPotName())
                .potStartDate(formatDate(entity.getPotStartDate()))
                .potEndDate(formatDate(entity.getPotEndDate()))
                .potDuration(entity.getPotDuration())
                .potLan(entity.getPotLan())
                .potContent(entity.getPotContent())
                .potStatus(entity.getPotStatus())
                .potModeOfOperation(entity.getPotModeOfOperation().name())
                .potSummary(entity.getPotSummary())
                .recruitmentDeadline(entity.getRecruitmentDeadline())
                .recruitmentDetails(recruitmentDetails.stream().map(r -> PotRecruitmentResponseDto.builder()
                        .recruitmentId(r.getRecruitmentId())
                        .recruitmentRole(r.getRecruitmentRole().name())
                        .recruitmentCount(r.getRecruitmentCount())
                        .build()).collect(Collectors.toList()))
                .build();
    }

    private String formatDate(java.time.LocalDate date) {
        return (date != null) ? date.format(DATE_FORMATTER) : "N/A";
    }

}