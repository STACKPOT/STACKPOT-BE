package stackpot.stackpot.todo.dto;

import lombok.*;
import stackpot.stackpot.todo.entity.enums.TodoStatus;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPotTodoRequestDTO {
    private String content;
    private TodoStatus status;
}
