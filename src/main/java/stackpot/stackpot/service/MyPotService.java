package stackpot.stackpot.service;

import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.Taskboard;
import stackpot.stackpot.domain.mapping.UserTodo;
import stackpot.stackpot.web.dto.*;

import java.util.List;
import java.util.Map;

public interface MyPotService {

    // 사용자의 진행 중인 팟 조회
    Map<String, List<MyPotResponseDTO.OngoingPotsDetail>> getMyOnGoingPots();


    // 사용자의 특정 팟에서의 생성
    List<MyPotTodoResponseDTO> postTodo(Long potId, MyPotTodoRequestDTO requestDTO);

    List<MyPotTodoResponseDTO> getTodo(Long potId);

    List<MyPotTodoResponseDTO> updateTodos(Long potId, List<MyPotTodoUpdateRequestDTO> requestList);

    List<MyPotTodoResponseDTO> completeTodo(Long potId, Long todoId);

    MyPotTaskResponseDto creatTask(Long potId, MyPotTaskRequestDto.create request);
}