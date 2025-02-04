package stackpot.stackpot.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import stackpot.stackpot.domain.enums.TaskboardStatus;
import stackpot.stackpot.domain.mapping.Task;
import stackpot.stackpot.web.dto.*;

import java.util.List;
import java.util.Map;

public interface MyPotService {

    // 사용자의 진행 중인 팟 조회
    List<OngoingPotResponseDto> getMyPots();
    // 사용자의 특정 팟에서의 생성
    List<MyPotTodoResponseDTO> postTodo(Long potId, MyPotTodoRequestDTO requestDTO);

    Page<MyPotTodoResponseDTO> getTodo(Long potId, PageRequest pageRequest);

    List<MyPotTodoResponseDTO> updateTodos(Long potId, List<MyPotTodoUpdateRequestDTO> requestList);

    List<MyPotTodoResponseDTO> completeTodo(Long potId, Long todoId);

    MyPotTaskResponseDto creatTask(Long potId, MyPotTaskRequestDto.create request);

    public Map<TaskboardStatus, List<MyPotTaskPreViewResponseDto>> preViewTask(Long potId);
    MyPotTaskResponseDto viewDetailTask(Long taskId);
    MyPotTaskResponseDto modfiyTask(Long taskId, MyPotTaskRequestDto.create request);
    void deleteTaskboard(Long potId, Long taskboardId);
    CompletedPotDetailResponseDto getCompletedPotDetail(Long potId);
    List<CompletedPotBadgeResponseDto> getCompletedPotsWithBadges();
    List<CompletedPotBadgeResponseDto> getUserCompletedPotsWithBadges(Long userId);
    List<OngoingPotResponseDto> getMyOngoingPots();

    MyPotTaskStatusResponseDto updateTaskStatus(Long potId, Long taskId, TaskboardStatus status);

}