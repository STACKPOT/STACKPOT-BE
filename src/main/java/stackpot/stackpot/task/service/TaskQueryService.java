package stackpot.stackpot.task.service;

import stackpot.stackpot.pot.entity.mapping.PotMember;
import stackpot.stackpot.task.dto.*;
import stackpot.stackpot.task.entity.enums.TaskboardStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TaskQueryService {
    Map<TaskboardStatus, List<MyPotTaskPreViewResponseDto>> preViewTask(Long potId);
    MyPotTaskResponseDto viewDetailTask(Long potId, Long taskBoardId);
    List<MyPotTaskPreViewResponseDto> getTasksFromDate(Long potId, LocalDate date);
    List<MonthlyTaskDto> getMonthlyTasks(Long potId, int year, int month);
    List<PotMember> getTop2TaskCountByPotMemberId(List<Long> potMemberIds);
}
