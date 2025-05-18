package stackpot.stackpot.pot.entity;

import jakarta.persistence.*;
import lombok.*;
import stackpot.stackpot.common.BaseEntity;
import stackpot.stackpot.user.entity.enums.Role;


@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PotRecruitmentDetails extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    @Getter
    @Setter
    private Long recruitmentId;

    @Column(nullable = true, length = 255)
    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private Role recruitmentRole;

    @Column(nullable = true)
    @Getter
    @Setter
    private Integer recruitmentCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pot_id", nullable = false)
    private Pot pot;
}
