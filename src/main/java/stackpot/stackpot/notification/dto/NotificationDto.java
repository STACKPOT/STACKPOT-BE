package stackpot.stackpot.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import stackpot.stackpot.user.entity.enums.Role;

import java.time.LocalDateTime;

public class NotificationDto {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnReadNotificationDto {
        private Long notificationId;
        private Long potOrFeedId;
        private Role role;
        private String userName;
        private String type;
        private String content;
        private LocalDateTime createdAt;
    }
}
