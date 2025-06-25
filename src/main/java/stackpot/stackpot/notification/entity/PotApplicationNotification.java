package stackpot.stackpot.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import stackpot.stackpot.common.BaseEntity;
import stackpot.stackpot.pot.entity.mapping.PotApplication;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PotApplicationNotification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pot_application_notification_id")
    private Long id;

    private Boolean isRead;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pot_application_id")
    private PotApplication potApplication;

    public void updateIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
}
