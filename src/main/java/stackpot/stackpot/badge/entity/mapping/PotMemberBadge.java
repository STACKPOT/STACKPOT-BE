package stackpot.stackpot.badge.entity.mapping;

import jakarta.persistence.*;
import lombok.*;
import stackpot.stackpot.badge.entity.Badge;
import stackpot.stackpot.common.BaseEntity;
import stackpot.stackpot.pot.entity.mapping.PotMember;

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
    private Badge badge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pot_member_id", nullable = false)
    private PotMember potMember;
}
