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
            "pcn.id, " +
            "p.potId, " +
            "null, " +
            "u.nickname, " +
            "'팟 댓글 알림', " +
            "CONCAT(u.nickname, ' 새싹님의 댓글이 달렸어요. ', pc.comment), " +
            "pcn.createdAt) " +
            "FROM PotCommentNotification pcn " +
            "JOIN pcn.potComment pc " +
            "JOIN pc.user u " +
            "JOIN pc.pot p " +
            "LEFT JOIN pc.parent pcp " +
            "WHERE pcn.isRead = false AND " +
            "((pc.parent is null AND p.user.id = :userId) OR " +
            " (pc.parent is not null AND (pcp.user.id = :userId OR p.user.id = :userId)))")
    List<NotificationDto.UnReadNotificationDto> findAllUnReadNotificationsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM PotCommentNotification pcn WHERE pcn.potComment.id = :potCommentId")
    void deleteByPotCommentId(@Param("potCommentId") Long potCommentId);
}
