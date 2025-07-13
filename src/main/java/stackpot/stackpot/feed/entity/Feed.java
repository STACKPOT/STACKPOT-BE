package stackpot.stackpot.feed.entity;

import jakarta.persistence.*;
import lombok.*;
import stackpot.stackpot.feed.entity.enums.Interest;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.common.BaseEntity;
import stackpot.stackpot.feed.entity.enums.Category;

import java.util.ArrayList;
import java.util.List;

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

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "feed_categories", joinColumns = @JoinColumn(name = "feed_id"))
    @Column(name = "category")
    private List<Category> categories = new ArrayList<>();

    @ElementCollection(targetClass = Interest.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "feed_interest", joinColumns = @JoinColumn(name = "feed_id"))
    @Column(name = "interest")
    private List<Interest> interests = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id", nullable = true)
    private Series series;


}
