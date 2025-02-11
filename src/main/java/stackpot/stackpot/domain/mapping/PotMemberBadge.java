package stackpot.stackpot.domain.mapping;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import stackpot.stackpot.domain.Badge;
import stackpot.stackpot.domain.common.BaseEntity;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PotMemberBadge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long potMemberBadgeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Badge badge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pot_member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PotMember potMember;
}
