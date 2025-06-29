package stackpot.stackpot.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stackpot.stackpot.notification.dto.NotificationDto;
import stackpot.stackpot.notification.entity.PotCommentNotification;

import java.util.List;

public interface PotCommentNotificationRepository extends JpaRepository<PotCommentNotification, Long> {

    @Query("SELECT new stackpot.stackpot.notification.dto.NotificationDto$UnReadNotificationDto(" +
            "pcn.id, pcn.potComment.pot.potId, pcn.potComment.user.nickname, 'PotComment', pcn.potComment.comment, pcn.createdAt) " +
            "FROM PotCommentNotification pcn " +
            "WHERE pcn.isRead = false AND (" +
            "     (pcn.potComment.parent is null AND pcn.potComment.pot.user.id = :userId) OR " +
            "     (pcn.potComment.parent is not null AND " +
            "         (pcn.potComment.parent.user.id = :userId OR pcn.potComment.pot.user.id = :userId)" +
            "     ))")
    List<NotificationDto.UnReadNotificationDto> findAllUnReadNotificationsByUserId(@Param("userId") Long userId);
}
