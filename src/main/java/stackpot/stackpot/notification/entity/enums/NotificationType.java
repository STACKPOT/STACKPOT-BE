package stackpot.stackpot.notification.entity.enums;

import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.NotificationHandler;
import stackpot.stackpot.notification.service.NotificationCommandService;

public enum NotificationType {
    POT_APPLICATION {
        @Override
        public void read(Long id, NotificationCommandService service) {
            service.getPotApplicationNotificationRepository().findById(id)
                    .orElseThrow(() -> new NotificationHandler(ErrorStatus.NOTIFICATION_NOT_FOUND))
                    .updateIsRead(true);
        }
    },
    POT_COMMENT {
        @Override
        public void read(Long id, NotificationCommandService service) {
            service.getPotCommentNotificationRepository().findById(id)
                    .orElseThrow(() -> new NotificationHandler(ErrorStatus.NOTIFICATION_NOT_FOUND))
                    .updateIsRead(true);
        }
    },
    FEED_LIKE {
        @Override
        public void read(Long id, NotificationCommandService service) {
            service.getFeedLikeNotificationRepository().findById(id)
                    .orElseThrow(() -> new NotificationHandler(ErrorStatus.NOTIFICATION_NOT_FOUND))
                    .updateIsRead(true);
        }
    },
    FEED_COMMENT {
        @Override
        public void read(Long id, NotificationCommandService service) {
            service.getFeedCommentNotificationRepository().findById(id)
                    .orElseThrow(() -> new NotificationHandler(ErrorStatus.NOTIFICATION_NOT_FOUND))
                    .updateIsRead(true);
        }
    };

    public abstract void read(Long id, NotificationCommandService service);

    public static NotificationType from(String type) {
        return switch (type) {
            case "PotApplication" -> POT_APPLICATION;
            case "PotComment" -> POT_COMMENT;
            case "FeedLike" -> FEED_LIKE;
            case "FeedComment" -> FEED_COMMENT;
            default -> throw new NotificationHandler(ErrorStatus.INVALID_NOTIFICATION_TYPE);
        };
    }
}
