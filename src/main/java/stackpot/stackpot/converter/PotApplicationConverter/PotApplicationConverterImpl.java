package stackpot.stackpot.converter.PotApplicationConverter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.domain.mapping.PotApplication;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.ApplicationStatus;
import stackpot.stackpot.web.dto.PotApplicationRequestDto;
import stackpot.stackpot.web.dto.PotApplicationResponseDto;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class PotApplicationConverterImpl implements PotApplicationConverter {

    @Override
    public PotApplication toEntity(PotApplicationRequestDto dto, Pot pot, User user) {
        // User와 Pot 객체가 null인지 확인
        if (pot == null || user == null) {
            throw new IllegalArgumentException("Pot or User cannot be null");
        }

        return PotApplication.builder()
                .pot(pot)
                .user(user)
                .potRole(Role.valueOf(dto.getPotRole()))
                .liked(false) // 기본값 false
                .status(ApplicationStatus.PENDING) // 지원 상태
                .appliedAt(LocalDateTime.now()) // 지원 시간
                .build();
    }

    @Override
    public PotApplicationResponseDto toDto(PotApplication entity) {

        String appliedRole = getVegetableNameByRole(entity.getPotRole().name());

        return PotApplicationResponseDto.builder()
                .applicationId(entity.getApplicationId())
                .potRole(entity.getPotRole().name())
                // .status(entity.getStatus() != null ? entity.getStatus().name() : "UNKNOWN")
                .userId(entity.getUser().getId())
                .userNickname(entity.getUser().getNickname() + appliedRole)
                .build();
    }

    private String getVegetableNameByRole(String role) {
        Map<String, String> roleToVegetableMap = Map.of(
                "DESIGN", " 브로콜리",
                "PLANNING", " 당근",
                "BACKEND", " 양파",
                "FRONTEND", " 버섯",
                "UNKNOWN",""
        );

        return roleToVegetableMap.getOrDefault(role, "알 수 없음");
    }

}
