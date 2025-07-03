package stackpot.stackpot.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import stackpot.stackpot.notification.dto.NotificationDto;
import stackpot.stackpot.notification.entity.FeedLikeNotification;

import java.util.List;
import java.util.Optional;

public interface FeedLikeNotificationRepository extends JpaRepository<FeedLikeNotification, Long> {

    @Query("SELECT new stackpot.stackpot.notification.dto.NotificationDto$UnReadNotificationDto(" +
            "fln.id, fln.feedLike.feed.feedId, fln.feedLike.user.nickname, 'FeedLike', null, fln.createdAt) " +
            "FROM FeedLikeNotification fln " +
            "WHERE fln.isRead = false and fln.feedLike.feed.user.id = :userId ")
    List<NotificationDto.UnReadNotificationDto> findAllUnReadNotificationsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM FeedLikeNotification fln WHERE fln.feedLike.likeId = :feedLikeId")
    void deleteByFeedLikeId(@Param("feedLikeId") Long feedLikeId);
}
