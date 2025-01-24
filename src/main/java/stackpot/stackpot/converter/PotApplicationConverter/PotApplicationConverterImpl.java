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
        // Null 검사 및 예외 처리
        if (entity == null) {
            throw new IllegalArgumentException("PotApplication entity cannot be null");
        }
        if (entity.getPot() == null) {
            throw new IllegalArgumentException("Pot entity cannot be null in PotApplication");
        }
        if (entity.getUser() == null) {
            throw new IllegalArgumentException("User entity cannot be null in PotApplication");
        }

        return PotApplicationResponseDto.builder()
                .applicationId(entity.getApplicationId())
                .potRole(entity.getPotRole().name())
                .liked(entity.getLiked())
                .status(entity.getStatus() != null ? entity.getStatus().name() : "UNKNOWN") // 상태가 null이면 기본값 설정
                .appliedAt(entity.getAppliedAt()) // null 가능성을 허용
                .potId(entity.getPot().getPotId()) // Pot 엔티티에서 potId 가져오기
                .userId(entity.getUser().getId()) // User 엔티티에서 id 가져오기
                .build();
    }

}
