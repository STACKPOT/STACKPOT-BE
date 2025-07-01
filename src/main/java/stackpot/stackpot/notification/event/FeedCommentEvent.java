package stackpot.stackpot.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import stackpot.stackpot.notification.dto.NotificationResponseDto;

@Getter
@AllArgsConstructor
public class FeedCommentEvent {
    private Long feedWriterId;
    private Long parentCommentWriterId;
    private NotificationResponseDto.UnReadNotificationDto unReadNotificationDto;
}
