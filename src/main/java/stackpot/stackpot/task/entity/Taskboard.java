package stackpot.stackpot.task.entity;

import jakarta.persistence.*;
import lombok.*;
import stackpot.stackpot.common.BaseEntity;
import stackpot.stackpot.task.entity.enums.TaskboardStatus;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.user.entity.User;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Taskboard extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long taskboardId;

    @Column(nullable = false, length = 20)
    private String title;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskboardStatus status;

    @Column(nullable = false)
    private LocalDate deadLine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pot_id", nullable = false)
    private Pot pot;

    // TaskBoard 생성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
