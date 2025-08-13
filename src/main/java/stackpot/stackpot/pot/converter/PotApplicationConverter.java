package stackpot.stackpot.pot.converter;

import stackpot.stackpot.common.util.RoleNameMapper;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.entity.enums.ApplicationStatus;
import stackpot.stackpot.pot.entity.mapping.PotApplication;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.pot.dto.PotApplicationRequestDto;
import stackpot.stackpot.pot.dto.PotApplicationResponseDto;

import org.springframework.stereotype.Component;
import stackpot.stackpot.user.entity.enums.Role;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class PotApplicationConverter {

    public PotApplication toEntity(PotApplicationRequestDto dto, Pot pot, User user) {
        if (pot == null || user == null) {
            throw new IllegalArgumentException("Pot or User cannot be null");
        }

        return PotApplication.builder()
                .pot(pot)
                .user(user)
                .potRole(Role.valueOf(dto.getPotRole()))
                .liked(false)
                .status(ApplicationStatus.PENDING)
                .appliedAt(LocalDateTime.now())
                .build();
    }

    public PotApplicationResponseDto toDto(PotApplication entity) {
        String roleName = entity.getPotRole().name();
        String userNickname = RoleNameMapper.getWriterNickname(entity.getUser());
        Map<String, String> roleInfo = new HashMap<>();
        roleInfo.put("name", roleName);
        roleInfo.put("koreanName", Role.toKoreanName(roleName));

        return PotApplicationResponseDto.builder()
                .applicationId(entity.getApplicationId())
                .potRole(roleInfo)
                .userId(entity.getUser().getId())
                .userNickname(userNickname)
                .build();

    }

}
