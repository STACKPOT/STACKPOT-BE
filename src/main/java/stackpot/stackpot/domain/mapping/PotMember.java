package stackpot.stackpot.domain.mapping;

import jakarta.persistence.*;
import lombok.*;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.common.BaseEntity;
import stackpot.stackpot.domain.enums.Role;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PotMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long potMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pot_id", nullable = false)
    private Pot pot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private PotApplication potApplication;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role roleName;

    @Getter
    @Column(nullable = false)
    private boolean owner;

    @Setter
    @Getter
    @Column(nullable = true)
    private String appealContent;
}
