package stackpot.stackpot.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import stackpot.stackpot.notification.dto.NotificationResponseDto;

@Getter
@AllArgsConstructor
public class PotCommentEvent {
    private Long potLeaderId;
    private Long parentCommentWriterId;
    private NotificationResponseDto.UnReadNotificationDto unReadNotificationDto;
}
