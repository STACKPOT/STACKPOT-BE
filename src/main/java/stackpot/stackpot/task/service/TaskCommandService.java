package stackpot.stackpot.task.service;

import stackpot.stackpot.task.dto.*;
import stackpot.stackpot.task.entity.enums.TaskboardStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TaskCommandService {
    MyPotTaskResponseDto createTask(Long potId, MyPotTaskRequestDto.create request);
    void deleteTaskBoard(Long potId, Long taskBoardId);
    MyPotTaskStatusResponseDto updateTaskStatus(Long potId, Long taskId, TaskboardStatus status);
    MyPotTaskResponseDto modifyTask(Long potId, Long taskBoardId, MyPotTaskRequestDto.create request);
}
