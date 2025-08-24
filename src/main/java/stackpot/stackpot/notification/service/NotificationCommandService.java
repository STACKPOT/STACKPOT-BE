package stackpot.stackpot.notification.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.feed.entity.mapping.FeedComment;
import stackpot.stackpot.feed.entity.mapping.FeedLike;
import stackpot.stackpot.feed.service.FeedCommentQueryService;
import stackpot.stackpot.feed.service.FeedLikeQueryService;
import stackpot.stackpot.notification.converter.NotificationConverter;
import stackpot.stackpot.notification.dto.NotificationRequestDto;
import stackpot.stackpot.notification.dto.NotificationResponseDto;
import stackpot.stackpot.notification.entity.*;
import stackpot.stackpot.notification.entity.enums.NotificationType;
import stackpot.stackpot.notification.repository.*;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.entity.mapping.PotApplication;
import stackpot.stackpot.pot.entity.mapping.PotComment;
import stackpot.stackpot.pot.service.pot.PotQueryService;
import stackpot.stackpot.pot.service.potApplication.PotApplicationQueryService;
import stackpot.stackpot.pot.service.potComment.PotCommentQueryService;

@Service
@RequiredArgsConstructor
@Slf4j
@Getter
public class NotificationCommandService {

    private final NotificationQueryService notificationQueryService;
    private final PotApplicationQueryService potApplicationQueryService;
    private final PotCommentQueryService potCommentQueryService;
    private final FeedCommentQueryService feedCommentQueryService;
    private final FeedLikeQueryService feedLikeQueryService;
    private final PotQueryService potQueryService;

    private final PotApplicationNotificationRepository potApplicationNotificationRepository;
    private final PotCommentNotificationRepository potCommentNotificationRepository;
    private final FeedLikeNotificationRepository feedLikeNotificationRepository;
    private final FeedCommentNotificationRepository feedCommentNotificationRepository;
    private final PotEndNotificationRepository potEndNotificationRepository;

    private final NotificationConverter notificationConverter;
    private final AuthService authService;

    @Transactional
    public void readNotification(NotificationRequestDto.ReadNotificationDto readNotificationDto) {
        Long notificationId = readNotificationDto.getNotificationId();
        String notificationType = readNotificationDto.getNotificationType();
        NotificationType type = NotificationType.from(notificationType);
        type.read(notificationId, this);
    }

    public NotificationResponseDto.UnReadNotificationDto createPotApplicationNotification(Long potId, Long applicationId, String userName) {
        PotApplication potApplication = potApplicationQueryService.getPotApplicationById(applicationId);
        // 해당 유저가 Pot의 생성자일 경우 알림 생성하지 않음 -> 이미 PotApplication 생성 자체가 안 됨
        PotApplicationNotification pan = PotApplicationNotification.builder()
                .isRead(false)
                .potApplication(potApplication)
                .build();
        PotApplicationNotification newPan = potApplicationNotificationRepository.save(pan);

        // Pot의 생성자에게 실시간 알림 전송 필요
        return notificationConverter.toUnReadNotificationDto(
                newPan.getId(), potId, null, userName + "새싹", "팟 지원 알림",
                userName + "새싹님이 내 팟에 지원했어요. 확인해 보세요!", newPan.getCreatedAt());
    }

    @Transactional
    public void deletePotApplicationNotification(Long potApplicationId) {
        potApplicationNotificationRepository.deleteByPotApplicationId(potApplicationId);
    }

    public NotificationResponseDto.UnReadNotificationDto createPotCommentNotification(Long potId, Long commentId, Long userId) {
        PotComment potComment = potCommentQueryService.selectPotCommentByCommentId(commentId);
        if (potComment.getPot().getUser().getId().equals(userId)) {
            return null; // 해당 유저가 Pot의 생성자일 경우 알림 생성하지 않음
        }
        if (potComment.getParent() != null && potComment.getParent().getUser().getUserId().equals(userId)) {
            return null; // 해당 유저가 부모 댓글 생성자인 경우 알림 생성하지 않음
        }

        PotCommentNotification pcn = PotCommentNotification.builder()
                .isRead(false)
                .potComment(potComment)
                .build();
        PotCommentNotification newPcn = potCommentNotificationRepository.save(pcn);

        return notificationConverter.toUnReadNotificationDto(
                newPcn.getId(), potId, null, potComment.getUser().getNickname() + "새싹",
                "팟 댓글 알림", potComment.getUser().getNickname() + "새싹님의 댓글이 달렸어요." + potComment.getComment(), newPcn.getCreatedAt());
    }

    @Transactional
    public void deletePotCommentNotification(Long potCommentId) {
        potCommentNotificationRepository.deleteByPotCommentId(potCommentId);
    }

    public NotificationResponseDto.UnReadNotificationDto createFeedLikeNotification(Long feedId, Long feedLikeId, Long userId) {
        FeedLike feedLike = feedLikeQueryService.getFeedLikeById(feedLikeId);
        if (feedLike.getFeed().getUser().getId().equals(userId)) {
            return null; // 해당 유저가 Feed의 생성자일 경우 알림 생성하지 않음
        }
        FeedLikeNotification fln = FeedLikeNotification.builder()
                .isRead(false)
                .feedLike(feedLike)
                .build();
        FeedLikeNotification newFln = feedLikeNotificationRepository.save(fln);

        return notificationConverter.toUnReadNotificationDto(
                newFln.getId(), null, feedId, feedLike.getUser().getNickname() + "새싹",
                "피드 좋아요 알림", feedLike.getUser().getNickname() + "새싹님이 내 피드에 좋아요를 눌렀어요. 확인해 보세요!", newFln.getCreatedAt());
    }

    // todo toggleLike 메서드에 구현해야
    @Transactional
    public void deleteFeedLikeNotification(Long feedLikeId) {
        feedLikeNotificationRepository.deleteByFeedLikeId(feedLikeId);
    }

    public NotificationResponseDto.UnReadNotificationDto createdFeedCommentNotification(Long feedId, Long commentId, Long userId) {
        FeedComment feedComment = feedCommentQueryService.selectFeedCommentByCommentId(commentId);
        if (feedComment.getFeed().getUser().getId().equals(userId)) {
            return null; // 해당 유저가 Feed의 생성자일 경우 알림 생성하지 않음
        }
        if (feedComment.getParent() != null && feedComment.getParent().getUser().getUserId().equals(userId)) {
            return null; // 해당 유저가 부모 댓글 생성자인 경우 알림 생성하지 않음
        }
        FeedCommentNotification fcn = FeedCommentNotification.builder()
                .isRead(false)
                .feedComment(feedComment)
                .build();
        FeedCommentNotification newFcn = feedCommentNotificationRepository.save(fcn);

        return notificationConverter.toUnReadNotificationDto(
                newFcn.getId(), null, feedId, feedComment.getUser().getNickname() + "새싹",
                "피드 댓글 알림", feedComment.getUser().getNickname() + "새싹님의 댓글이 달렸어요." + feedComment.getComment(), newFcn.getCreatedAt());
    }

    @Transactional
    public void deleteFeedCommentNotification(Long feedCommentId) {
        feedCommentNotificationRepository.deleteByFeedCommentId(feedCommentId);
    }

    public NotificationResponseDto.UnReadNotificationDto createdPotEndNotification(Long potId) {
        Pot pot = potQueryService.getPotByPotId(potId);
        PotEndNotification pen = PotEndNotification.builder()
                .isRead(false)
                .pot(pot)
                .build();
        PotEndNotification newPen = potEndNotificationRepository.save(pen);
        return notificationConverter.toUnReadNotificationDto(
                newPen.getId(), potId, null, pot.getUser().getNickname() + "새싹",
                "팟 종료 알림", pot.getUser().getNickname() + "이 다 끓었어요. 내 역할을 소개해 보세요!", newPen.getCreatedAt());
    }

    // 삭제할 일은 없네
    @Transactional
    public void deletePotEndNotification(Long potId) {
        potEndNotificationRepository.deleteByPotId(potId);
    }
}
