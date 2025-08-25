package stackpot.stackpot.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import stackpot.stackpot.common.BaseEntity;
import stackpot.stackpot.pot.entity.Pot;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PotEndNotification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pot_end_notification_id")
    private Long id;

    private Boolean isRead;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pot_id")
    private Pot pot;

    public void updateIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
}
