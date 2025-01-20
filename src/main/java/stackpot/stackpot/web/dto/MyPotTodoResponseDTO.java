package stackpot.stackpot.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import stackpot.stackpot.domain.enums.TodoStatus;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPotTodoResponseDTO {

        private Long potId;
        private Long todoId;
        private String content;
        private TodoStatus status;
}
