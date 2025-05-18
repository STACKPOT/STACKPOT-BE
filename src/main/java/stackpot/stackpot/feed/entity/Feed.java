package stackpot.stackpot.feed.entity;

import jakarta.persistence.*;
import lombok.*;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.common.BaseEntity;
import stackpot.stackpot.feed.entity.enums.Category;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Feed extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private long likeCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;
}
