package stackpot.stackpot.todo.dto;

import lombok.*;
import stackpot.stackpot.todo.entity.enums.TodoStatus;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPotTodoUpdateRequestDTO {
    private Long todoId;
    private String content;
    private TodoStatus status;
}
