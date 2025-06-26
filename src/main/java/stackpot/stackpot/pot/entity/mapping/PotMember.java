package stackpot.stackpot.pot.entity.mapping;

import jakarta.persistence.*;
import lombok.*;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.common.BaseEntity;
import stackpot.stackpot.user.entity.enums.Role;

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
    @JoinColumn(name = "application_id", nullable = true)
    private PotApplication potApplication;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 10)
    private Role roleName;

    @Getter
    @Column(nullable = false)
    private boolean owner;

    @Setter
    @Getter
    @Column(nullable = true)
    private String appealContent;

    public void updateOwner(boolean newOwnerStatus) {
        this.owner = newOwnerStatus;
    }

    public void deletePotMember() {
        this.roleName = Role.UNKNOWN;
    }

    public void setApplication(PotApplication application) {
        this.potApplication = application;
    }
}
