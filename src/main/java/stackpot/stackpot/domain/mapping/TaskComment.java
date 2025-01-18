package stackpot.stackpot.domain.mapping;

import jakarta.persistence.*;
import lombok.*;
import stackpot.stackpot.domain.Taskboard;
import stackpot.stackpot.domain.common.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TaskComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long commentId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment_content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "potMember_id", nullable = false)
    private PotMember potMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taskboard_id", nullable = false)
    private Taskboard taskboard;
}
