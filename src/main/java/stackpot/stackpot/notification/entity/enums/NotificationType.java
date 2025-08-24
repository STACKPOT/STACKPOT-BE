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
    },
    POT_END {
      @Override
      public void read(Long id, NotificationCommandService service){
            service.getPotEndNotificationRepository().findById(id)
                    .orElseThrow(() -> new NotificationHandler(ErrorStatus.NOTIFICATION_NOT_FOUND))
                    .updateIsRead(true);
      }
    };

    public abstract void read(Long id, NotificationCommandService service);

    public static NotificationType from(String type) {
        return switch (type) {
            case "팟 지원 알림" -> POT_APPLICATION;
            case "팟 댓글 알림" -> POT_COMMENT;
            case "피드 좋아요 알림" -> FEED_LIKE;
            case "피드 댓글 알림" -> FEED_COMMENT;
            case "팟 종료 알림" -> POT_END;
            default -> throw new NotificationHandler(ErrorStatus.INVALID_NOTIFICATION_TYPE);
        };
    }
}
