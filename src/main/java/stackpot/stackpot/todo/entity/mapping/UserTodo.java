package stackpot.stackpot.todo.entity.mapping;

import jakarta.persistence.*;
import lombok.*;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.common.BaseEntity;
import stackpot.stackpot.todo.entity.enums.TodoStatus;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserTodo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long todoId;

    @Column(nullable = false, length = 50)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TodoStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pot_id", nullable = false)
    private Pot pot;
}
