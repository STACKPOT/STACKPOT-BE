package stackpot.stackpot.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import stackpot.stackpot.chat.service.event.SseService;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.notification.converter.NotificationConverter;
import stackpot.stackpot.notification.dto.NotificationDto;
import stackpot.stackpot.notification.dto.NotificationResponseDto;
import stackpot.stackpot.notification.repository.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationQueryService {

    private final PotApplicationNotificationRepository potApplicationNotificationRepository;
    private final PotCommentNotificationRepository potCommentNotificationRepository;
    private final FeedLikeNotificationRepository feedLikeNotificationRepository;
    private final FeedCommentNotificationRepository feedCommentNotificationRepository;

    private final NotificationConverter notificationConverter;
    private final AuthService authService;
    private final SseService sseService;

    public List<NotificationResponseDto.UnReadNotificationDto> getAllUnReadNotifications() {
        Long userId = authService.getCurrentUserId();

        // 1. PotApplicationNotification : potApplication - pot - user.getId() == userId
        List<NotificationDto.UnReadNotificationDto> potApplicationNotifications =
                potApplicationNotificationRepository.findAllUnReadNotificationsByUserId(userId);

        // 3. PotCommentNotification : potComment - pot - user.getId() == userId && 부모 댓글의 userId == userId
        List<NotificationDto.UnReadNotificationDto> potCommentNotifications =
                potCommentNotificationRepository.findAllUnReadNotificationsByUserId(userId);

        // 4. FeedLikeNotification : feedLike - feed - user.getId() == userId
        List<NotificationDto.UnReadNotificationDto> feedLikeNotifications =
                feedLikeNotificationRepository.findAllUnReadNotificationsByUserId(userId);

        // 5. FeedCommentNotification : feedComment - feed - user.getId() == userId && 부모 댓글의 userId == userId
        List<NotificationDto.UnReadNotificationDto> feedCommentNotifications =
                feedCommentNotificationRepository.findAllUnReadNotificationsByUserId(userId);

        List<NotificationDto.UnReadNotificationDto> temp = Stream.of(
                        potApplicationNotifications,
                        potCommentNotifications,
                        feedLikeNotifications,
                        feedCommentNotifications)
                .flatMap(List::stream)
                .toList();

        // 내림차순
        return temp.stream()
                .map(notificationConverter::toUnReadNotificationDto)
                .sorted(Comparator.comparing(NotificationResponseDto.UnReadNotificationDto::getCreatedAt).reversed())
                .toList();
    }
}
