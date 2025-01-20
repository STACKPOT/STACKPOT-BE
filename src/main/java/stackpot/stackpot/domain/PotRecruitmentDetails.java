package stackpot.stackpot.domain;

import jakarta.persistence.*;
import lombok.*;
import stackpot.stackpot.domain.common.BaseEntity;

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
    private String recruitmentRole;

    @Column(nullable = true)
    @Getter
    @Setter
    private Integer recruitmentCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pot_id", nullable = false)
    private Pot pot;
}
