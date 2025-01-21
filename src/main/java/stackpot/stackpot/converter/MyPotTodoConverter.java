package stackpot.stackpot.converter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.domain.mapping.UserTodo;
import stackpot.stackpot.web.dto.MyPotTodoResponseDTO;

@Component
public class MyPotTodoConverter {
    public static MyPotTodoResponseDTO toTodoResultDto(UserTodo userTodo){
        return MyPotTodoResponseDTO.builder()
                .potId(userTodo.getPot().getPotId())
                .todoId(userTodo.getTodoId())
                .content(userTodo.getContent())
                .status(userTodo.getStatus())
                .build();
    }
}
