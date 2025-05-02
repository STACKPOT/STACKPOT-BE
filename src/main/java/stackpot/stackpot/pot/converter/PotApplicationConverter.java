package stackpot.stackpot.pot.converter;

import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.entity.enums.ApplicationStatus;
import stackpot.stackpot.pot.entity.mapping.PotApplication;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.pot.dto.PotApplicationRequestDto;
import stackpot.stackpot.pot.dto.PotApplicationResponseDto;

import org.springframework.stereotype.Component;
import stackpot.stackpot.user.entity.enums.Role;


import java.time.LocalDateTime;
import java.util.Map;

@Component
public class PotApplicationConverter{

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