package stackpot.stackpot.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import stackpot.stackpot.notification.dto.NotificationResponseDto;

@Getter
@AllArgsConstructor
public class FeedLikeEvent {
    private Long feedWriterId;
    private NotificationResponseDto.UnReadNotificationDto unReadNotificationDto;
}
