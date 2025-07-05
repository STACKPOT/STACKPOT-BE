package stackpot.stackpot.notification.converter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.notification.dto.NotificationDto;
import stackpot.stackpot.notification.dto.NotificationResponseDto;

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
                .userName(unReadNotificationDto.getUserName())
                .type(unReadNotificationDto.getType())
                .content(unReadNotificationDto.getContent())
                .createdAt(unReadNotificationDto.getCreatedAt().format(DATE_FORMATTER))
                .build();
    }

    public NotificationResponseDto.UnReadNotificationDto toUnReadNotificationDto(
            Long notificationId, Long potOrFeedId, String userName, String type, String content, LocalDateTime createdAt) {
        return NotificationResponseDto.UnReadNotificationDto.builder()
                .notificationId(notificationId)
                .potOrFeedId(potOrFeedId)
                .userName(userName)
                .type(type)
                .content(content)
                .createdAt(createdAt.format(DATE_FORMATTER))
                .build();
    }
}
