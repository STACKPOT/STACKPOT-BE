package stackpot.stackpot.notification.converter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.notification.dto.NotificationDto;
import stackpot.stackpot.notification.dto.NotificationResponseDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class NotificationConverter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 M월 d일 H:mm");

    public NotificationResponseDto.UnReadNotificationDto toUnReadNotificationDto(NotificationDto.UnReadNotificationDto unReadNotificationDto) {
        return NotificationResponseDto.UnReadNotificationDto.builder()
                .notificationId(unReadNotificationDto.getNotificationId())
                .potId(unReadNotificationDto.getPotId())
                .feedId(unReadNotificationDto.getFeedId())
                .userName(unReadNotificationDto.getUserName() == null ? " " : unReadNotificationDto.getUserName() + " 새싹")
                .type(unReadNotificationDto.getType())
                .content(unReadNotificationDto.getContent())
                .createdAt(unReadNotificationDto.getCreatedAt().format(DATE_FORMATTER))
                .build();
    }

    public NotificationResponseDto.UnReadNotificationDto toUnReadNotificationDto(
            Long notificationId, Long potId, Long feedId, String userName, String type, String content, LocalDateTime createdAt) {
        return NotificationResponseDto.UnReadNotificationDto.builder()
                .notificationId(notificationId)
                .potId(potId)
                .feedId(feedId)
                .userName(userName + " 새싹")
                .type(type)
                .content(content)
                .createdAt(createdAt.format(DATE_FORMATTER))
                .build();
    }
}
