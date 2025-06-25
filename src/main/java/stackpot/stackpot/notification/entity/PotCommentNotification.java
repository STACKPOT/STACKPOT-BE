package stackpot.stackpot.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import stackpot.stackpot.common.BaseEntity;
import stackpot.stackpot.pot.entity.mapping.PotComment;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PotCommentNotification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pot_comment_notification_id")
    private Long id;

    private Boolean isRead;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pot_comment_id")
    private PotComment potComment;

    public void updateIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
}
