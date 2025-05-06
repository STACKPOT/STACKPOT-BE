package stackpot.stackpot.todo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import stackpot.stackpot.todo.dto.MyPotTodoResponseDTO;
import stackpot.stackpot.todo.dto.MyPotTodoUpdateRequestDTO;

import java.util.List;

public interface UserTodoService {
    Page<MyPotTodoResponseDTO> getTodo(Long potId, PageRequest pageRequest);
    List<MyPotTodoResponseDTO> updateTodos(Long potId, List<MyPotTodoUpdateRequestDTO> requestList);
    List<MyPotTodoResponseDTO> completeTodo(Long potId, Long todoId);
}

