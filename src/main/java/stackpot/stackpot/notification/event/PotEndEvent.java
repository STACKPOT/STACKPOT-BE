package stackpot.stackpot.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import stackpot.stackpot.notification.dto.NotificationResponseDto;

@Getter
@AllArgsConstructor
public class PotEndEvent {
    private Long potId;
    private NotificationResponseDto.UnReadNotificationDto unReadNotificationDto;
}
