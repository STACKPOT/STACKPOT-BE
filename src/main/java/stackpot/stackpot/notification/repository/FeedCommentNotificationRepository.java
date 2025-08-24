package stackpot.stackpot.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stackpot.stackpot.notification.dto.NotificationDto;
import stackpot.stackpot.notification.entity.FeedCommentNotification;

import java.util.List;

public interface FeedCommentNotificationRepository extends JpaRepository<FeedCommentNotification, Long> {

    @Query("SELECT new stackpot.stackpot.notification.dto.NotificationDto$UnReadNotificationDto(" +
            "fcn.id, " +
            "null, " +
            "f.feedId, " +
            "u.nickname, " +
            "'피드 댓글 알림', " +
            "CONCAT(u.nickname, ' 새싹님의 댓글이 달렸어요. ', fc.comment), " +
            "fcn.createdAt) " +
            "FROM FeedCommentNotification fcn " +
            "JOIN fcn.feedComment fc " +
            "JOIN fc.user u " +
            "JOIN fc.feed f " +
            "LEFT JOIN fc.parent fcp " +
            "WHERE fcn.isRead = false AND " +
            "((fc.parent is null AND f.user.id = :userId) OR " +
            " (fc.parent is not null AND (fcp.user.id = :userId OR f.user.id = :userId)))")
    List<NotificationDto.UnReadNotificationDto> findAllUnReadNotificationsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM FeedCommentNotification fcn WHERE fcn.feedComment.id = :feedCommentId")
    void deleteByFeedCommentId(@Param("feedCommentId") Long feedCommentId);
}
