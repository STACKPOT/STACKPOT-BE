package stackpot.stackpot.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stackpot.stackpot.notification.dto.NotificationDto;
import stackpot.stackpot.notification.entity.PotApplicationNotification;

import java.util.List;

public interface PotApplicationNotificationRepository extends JpaRepository<PotApplicationNotification, Long> {

    @Query("SELECT new stackpot.stackpot.notification.dto.NotificationDto$UnReadNotificationDto(" +
            "pan.id, pan.potApplication.pot.potId, pan.potApplication.user.nickname, 'PotApplication', null, pan.createdAt) " +
            "FROM PotApplicationNotification pan " +
            "WHERE pan.isRead = false AND pan.potApplication.pot.user.id = :userId")
    List<NotificationDto.UnReadNotificationDto> findAllUnReadNotificationsByUserId(@Param("userId") Long userId);
}
