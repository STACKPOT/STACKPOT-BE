package stackpot.stackpot.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stackpot.stackpot.notification.dto.NotificationDto;
import stackpot.stackpot.notification.entity.PotEndNotification;

import java.util.List;

public interface PotEndNotificationRepository extends JpaRepository<PotEndNotification, Long> {

    @Query("SELECT new stackpot.stackpot.notification.dto.NotificationDto$UnReadNotificationDto(" +
            "pen.id, " +
            "pen.pot.potId, " +
            "null, " +
            "null, " +
            "'팟 종료 알림', " +
            "CONCAT(pen.pot.potName, '이 다 끓었어요. 내 역할을 소개해 보세요!'), " +
            "pen.createdAt) " +
            "FROM PotEndNotification pen " +
            "JOIN pen.pot.potMembers pm " +
            "WHERE pen.isRead = false AND pm.user.id = :userId")
    List<NotificationDto.UnReadNotificationDto> findAllUnReadPotEndNotificationsByUserId(@Param("userId") Long userId);

//    @Query("SELECT new stackpot.stackpot.notification.dto.NotificationDto$UnReadNotificationDto(" +
//            "pen.id, " +
//            "pen.pot.potId, " +
//            "null, " +
//            "pen.pot.potName, " +
//            "'팟 종료 알림', " +
//            "CONCAT(pen.pot.potName, '이 다 끓었어요. 내 역할을 소개해 보세요!'), " +
//            "pen.createdAt) " +
//            "FROM PotEndNotification pen " +
//            "JOIN pen.pot p " +
//            "JOIN p.potMembers pm " +
//            "WHERE pen.isRead = false AND pm.user.id = :userId")
//    List<NotificationDto.UnReadNotificationDto> findAllUnReadPotEndNotificationsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM PotEndNotification pen WHERE pen.pot.potId = :potId")
    void deleteByPotId(@Param("potId") Long potId);
}
