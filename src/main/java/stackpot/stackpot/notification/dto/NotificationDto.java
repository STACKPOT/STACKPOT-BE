package stackpot.stackpot.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class NotificationDto {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnReadNotificationDto {
        private Long id;
        private String userName;
        private String type;
        private String content;
        private LocalDateTime createdAt;
    }
}
