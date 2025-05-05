package stackpot.stackpot.task.service;

import stackpot.stackpot.task.dto.*;
import stackpot.stackpot.task.entity.enums.TaskboardStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TaskService {
    MyPotTaskResponseDto creatTask(Long potId, MyPotTaskRequestDto.create request);
    Map<TaskboardStatus, List<MyPotTaskPreViewResponseDto>> preViewTask(Long potId);
    MyPotTaskResponseDto viewDetailTask(Long potId, Long taskBoardId);
    void deleteTaskBoard(Long potId, Long taskBoardId);
    MyPotTaskStatusResponseDto updateTaskStatus(Long potId, Long taskId, TaskboardStatus status);
    List<MyPotTaskPreViewResponseDto> getTasksFromDate(Long potId, LocalDate date);
    List<MonthlyTaskDto> getMonthlyTasks(Long potId, int year, int month);
    MyPotTaskResponseDto modifyTask(Long potId, Long taskBoardId, MyPotTaskRequestDto.create request);
}
