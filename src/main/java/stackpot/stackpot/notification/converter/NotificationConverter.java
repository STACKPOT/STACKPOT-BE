package stackpot.stackpot.notification.converter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.notification.dto.NotificationDto;
import stackpot.stackpot.notification.dto.NotificationResponseDto;

@Component
public class NotificationConverter {

    public NotificationResponseDto.UnReadNotificationDto toUnReadNotificationDto(NotificationDto.UnReadNotificationDto unReadNotificationDto) {
        return NotificationResponseDto.UnReadNotificationDto.builder()
                .id(unReadNotificationDto.getId())
                .userName(unReadNotificationDto.getUserName())
                .type(unReadNotificationDto.getType())
                .content(unReadNotificationDto.getContent())
                .createdAt(unReadNotificationDto.getCreatedAt())
                .build();
    }
}
