package stackpot.stackpot.web.dto;

import lombok.*;
import stackpot.stackpot.domain.enums.TodoStatus;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPotTodoResponseDTO {

        private String userNickname;
        private Long userId;
        private Integer todoCount;
        private List<TodoDetailDTO> todos;


        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class TodoDetailDTO {
                private Long todoId;
                private String content;
                private TodoStatus status;
        }
}


