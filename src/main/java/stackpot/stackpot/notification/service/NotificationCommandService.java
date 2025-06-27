package stackpot.stackpot.notification.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.feed.entity.mapping.FeedComment;
import stackpot.stackpot.feed.entity.mapping.FeedLike;
import stackpot.stackpot.feed.service.FeedCommentQueryService;
import stackpot.stackpot.feed.service.FeedLikeQueryService;
import stackpot.stackpot.notification.dto.NotificationRequestDto;
import stackpot.stackpot.notification.dto.NotificationResponseDto;
import stackpot.stackpot.notification.entity.FeedCommentNotification;
import stackpot.stackpot.notification.entity.FeedLikeNotification;
import stackpot.stackpot.notification.entity.PotApplicationNotification;
import stackpot.stackpot.notification.entity.PotCommentNotification;
import stackpot.stackpot.notification.entity.enums.NotificationType;
import stackpot.stackpot.notification.repository.FeedCommentNotificationRepository;
import stackpot.stackpot.notification.repository.FeedLikeNotificationRepository;
import stackpot.stackpot.notification.repository.PotApplicationNotificationRepository;
import stackpot.stackpot.notification.repository.PotCommentNotificationRepository;
import stackpot.stackpot.pot.entity.mapping.PotApplication;
import stackpot.stackpot.pot.entity.mapping.PotComment;
import stackpot.stackpot.pot.service.potApplication.PotApplicationQueryService;
import stackpot.stackpot.pot.service.potComment.PotCommentQueryService;

@Service
@RequiredArgsConstructor
@Slf4j
@Getter
public class NotificationCommandService {

    private final PotApplicationQueryService potApplicationQueryService;
    private final PotCommentQueryService potCommentQueryService;
    private final FeedCommentQueryService feedCommentQueryService;
    private final FeedLikeQueryService feedLikeQueryService;

    private final PotApplicationNotificationRepository potApplicationNotificationRepository;
    private final PotCommentNotificationRepository potCommentNotificationRepository;
    private final FeedLikeNotificationRepository feedLikeNotificationRepository;
    private final FeedCommentNotificationRepository feedCommentNotificationRepository;

    private final AuthService authService;

    public void readNotification(NotificationRequestDto.ReadNotificationDto readNotificationDto) {
        Long notificationId = readNotificationDto.getNotificationId();
        String notificationType = readNotificationDto.getNotificationType();
        NotificationType type = NotificationType.from(notificationType);
        type.read(notificationId, this);
    }

    public NotificationResponseDto.UnReadNotificationDto createPotApplicationNotification(Long applicationId, String userName) {
        PotApplication potApplication = potApplicationQueryService.getPotApplicationById(applicationId);
        // 해당 유저가 Pot의 생성자일 경우 알림 생성하지 않음 -> 이미 PotApplication 생성 자체가 안 됨
        PotApplicationNotification pan = PotApplicationNotification.builder()
                .isRead(false)
                .potApplication(potApplication)
                .build();
        PotApplicationNotification newPan = potApplicationNotificationRepository.save(pan);

        // Pot의 생성자에게 실시간 알림 전송 필요
        return NotificationResponseDto.UnReadNotificationDto.builder()
                .id(newPan.getId())
                .userName(userName)
                .type("PotApplication")
                .content(null)
                .createdAt(newPan.getCreatedAt())
                .build();
    }

    public void createPotCommentNotification(Long commentId, Long userId) {
        PotComment potComment = potCommentQueryService.selectPotCommentByCommentId(commentId);
        if (potComment.getPot().getUser().getId().equals(userId) || potComment.getParent().getUser().getUserId().equals(userId)) {
            return; // 해당 유저가 팟 생성자이거나 부모 댓글 생성자인 경우 알림 생성하지 않음
        }
        PotCommentNotification pcn = PotCommentNotification.builder()
                .isRead(false)
                .potComment(potComment)
                .build();
        potCommentNotificationRepository.save(pcn);
    }

    public void createFeedLikeNotification(Long feedLikeId, Long userId) {
        FeedLike feedLike = feedLikeQueryService.getFeedLikeById(feedLikeId);
        if (feedLike.getFeed().getUser().getId().equals(userId)) {
            return; // 해당 유저가 Feed의 생성자일 경우 알림 생성하지 않음
        }
        FeedLikeNotification fln = FeedLikeNotification.builder()
                .isRead(false)
                .feedLike(feedLike)
                .build();
        feedLikeNotificationRepository.save(fln);
    }

    public void createdFeedCommentNotification(Long commentId, Long userId) {
        FeedComment feedComment = feedCommentQueryService.selectFeedCommentByCommentId(commentId);
        if (feedComment.getFeed().getUser().getId().equals(userId) || feedComment.getParent().getUser().getUserId().equals(userId)) {
            return; // 해당 유저가 Feed의 생성자이거나 부모 댓글 생성자인 경우 알림 생성하지 않음
        }
        FeedCommentNotification fcn = FeedCommentNotification.builder()
                .isRead(false)
                .feedComment(feedComment)
                .build();
        feedCommentNotificationRepository.save(fcn);
    }
}
