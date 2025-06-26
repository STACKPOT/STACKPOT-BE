package stackpot.stackpot.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import stackpot.stackpot.common.BaseEntity;
import stackpot.stackpot.feed.entity.mapping.FeedLike;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FeedLikeNotification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feed_like_notification_id")
    private Long id;

    private Boolean isRead;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "like_id")
    private FeedLike feedLike;

    public void updateIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
}
