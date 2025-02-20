package stackpot.stackpot.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.domain.enums.TodoStatus;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPotTodoResponseDTO {

        private String userNickname;
        private String userRole;
        private Long userId;
        @JsonInclude(JsonInclude.Include.NON_NULL)
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


