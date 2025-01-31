package stackpot.stackpot.web.dto;

import lombok.*;
import stackpot.stackpot.domain.enums.TodoStatus;


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
