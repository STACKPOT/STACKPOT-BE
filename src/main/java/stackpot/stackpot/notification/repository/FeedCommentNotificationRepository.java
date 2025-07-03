package stackpot.stackpot.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stackpot.stackpot.notification.dto.NotificationDto;
import stackpot.stackpot.notification.entity.FeedCommentNotification;

import java.util.List;
import java.util.Optional;

public interface FeedCommentNotificationRepository extends JpaRepository<FeedCommentNotification, Long> {

    @Query("SELECT new stackpot.stackpot.notification.dto.NotificationDto$UnReadNotificationDto(" +
            "fcn.id, fcn.feedComment.feed.feedId, fcn.feedComment.user.nickname, 'FeedComment', fcn.feedComment.comment, fcn.createdAt) " +
            "FROM FeedCommentNotification fcn " +
            "WHERE fcn.isRead = false AND (" +
            "     (fcn.feedComment.parent is null AND fcn.feedComment.feed.user.id = :userId) OR " +
            "     (fcn.feedComment.parent is not null AND " +
            "         (fcn.feedComment.parent.user.id = :userId OR fcn.feedComment.feed.user.id = :userId)" +
            "     ))")
    List<NotificationDto.UnReadNotificationDto> findAllUnReadNotificationsByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM FeedCommentNotification fcn WHERE fcn.feedComment.id = :feedCommentId")
    void deleteByFeedCommentId(@Param("feedCommentId") Long feedCommentId);
}
