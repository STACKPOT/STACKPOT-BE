package stackpot.stackpot.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stackpot.stackpot.notification.dto.NotificationDto;
import stackpot.stackpot.notification.entity.PotCommentNotification;

import java.util.List;
import java.util.Optional;

public interface PotCommentNotificationRepository extends JpaRepository<PotCommentNotification, Long> {

    @Query("SELECT new stackpot.stackpot.notification.dto.NotificationDto$UnReadNotificationDto(" +
            "pcn.id, pcn.potComment.pot.potId, null, " +
            "CONCAT(pcn.potComment.user.nickname, '새싹'), " +
            "'팟 댓글 알림', " +
            "CONCAT(pcn.potComment.user.nickname, '새싹님의 댓글이 달렸어요.', pcn.potComment.comment), " +
            "pcn.createdAt) " +
            "FROM PotCommentNotification pcn " +
            "WHERE pcn.isRead = false AND (" +
            "     (pcn.potComment.parent is null AND pcn.potComment.pot.user.id = :userId) OR " +
            "     (pcn.potComment.parent is not null AND " +
            "         (pcn.potComment.parent.user.id = :userId OR pcn.potComment.pot.user.id = :userId)" +
            "     ))")
    List<NotificationDto.UnReadNotificationDto> findAllUnReadNotificationsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM PotCommentNotification pcn WHERE pcn.potComment.id = :potCommentId")
    void deleteByPotCommentId(@Param("potCommentId") Long potCommentId);
}
