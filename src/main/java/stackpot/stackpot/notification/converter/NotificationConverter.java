package stackpot.stackpot.notification.converter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.notification.dto.NotificationDto;
import stackpot.stackpot.notification.dto.NotificationResponseDto;
import stackpot.stackpot.user.entity.enums.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class NotificationConverter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 M월 d일 H:mm");

    public NotificationResponseDto.UnReadNotificationDto toUnReadNotificationDto(NotificationDto.UnReadNotificationDto unReadNotificationDto) {
        return NotificationResponseDto.UnReadNotificationDto.builder()
                .notificationId(unReadNotificationDto.getNotificationId())
                .potOrFeedId(unReadNotificationDto.getPotOrFeedId())
                .role(unReadNotificationDto.getRole())
                .userName(unReadNotificationDto.getUserName() + " " + unReadNotificationDto.getRole().getVegetable())
                .type(unReadNotificationDto.getType())
                .content(unReadNotificationDto.getContent())
                .createdAt(unReadNotificationDto.getCreatedAt().format(DATE_FORMATTER))
                .build();
    }

    public NotificationResponseDto.UnReadNotificationDto toUnReadNotificationDto(
            Long notificationId, Long potOrFeedId, Role role, String userName, String type, String content, LocalDateTime createdAt) {
        return NotificationResponseDto.UnReadNotificationDto.builder()
                .notificationId(notificationId)
                .potOrFeedId(potOrFeedId)
                .role(role)
                .userName(userName + " " + role.getVegetable())
                .type(type)
                .content(content)
                .createdAt(createdAt.format(DATE_FORMATTER))
                .build();
    }
}
