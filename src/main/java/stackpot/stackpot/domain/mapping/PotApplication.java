package stackpot.stackpot.domain.mapping;

import jakarta.persistence.*;
import lombok.*;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.common.BaseEntity;
import stackpot.stackpot.domain.enums.ApplicationStatus;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(nullable = true)
    private LocalDateTime appliedAt;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean liked;

    @Column(nullable = false, length = 10)
    private String potRole; // 팟 역할

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pot_id", nullable = false)
    private Pot pot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    public void setApplicationStatus(ApplicationStatus status) {
        this.status = status;
    }

}
