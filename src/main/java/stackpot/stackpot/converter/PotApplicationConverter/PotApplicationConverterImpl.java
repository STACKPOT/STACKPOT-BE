package stackpot.stackpot.converter.PotApplicationConverter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.mapping.PotApplication;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.ApplicationStatus;
import stackpot.stackpot.web.dto.ApplicationRequestDto;
import stackpot.stackpot.web.dto.ApplicationResponseDto;

import java.time.LocalDateTime;

@Component
public class PotApplicationConverterImpl implements PotApplicationConverter {

    @Override
    public PotApplication toEntity(ApplicationRequestDto dto, Pot pot, User user) {
        return PotApplication.builder()
                .pot(pot)
                .user(user)
                .potRole(dto.getPotRole())
                .liked(dto.getLiked() != null ? dto.getLiked() : false)
                .status(ApplicationStatus.PENDING)
                .appliedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public ApplicationResponseDto toDto(PotApplication entity) {
        return ApplicationResponseDto.builder()
                .applicationId(entity.getApplicationId())
                .potRole(entity.getPotRole())
                .liked(entity.getLiked())
                .status(entity.getStatus().name())
                .appliedAt(entity.getAppliedAt())
                .potId(entity.getPot().getPotId())
                .userId(entity.getUser().getUserId())
                .build();
    }
}
