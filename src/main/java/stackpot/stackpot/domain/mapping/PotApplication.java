package stackpot.stackpot.domain.mapping;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.common.BaseEntity;
import stackpot.stackpot.domain.enums.ApplicationStatus;
import stackpot.stackpot.domain.enums.Role;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PotApplication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // IDENTITY로 자동 증가 설정
    @Column(name = "application_id", nullable = false)
    private Long applicationId; // Primary Key

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(nullable = true)
    private LocalDateTime appliedAt;

    @Setter
    @Column(nullable = false)
    @Builder.Default
    private Boolean liked = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role potRole; // 팟 역할

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pot_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Pot pot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    public void setApplicationStatus(ApplicationStatus status) {
        this.status = status;
    }

    @OneToOne(mappedBy = "potApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    private PotMember potMember;

}
