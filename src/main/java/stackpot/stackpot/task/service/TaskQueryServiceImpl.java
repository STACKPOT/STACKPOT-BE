package stackpot.stackpot.task.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.PotHandler;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.entity.mapping.PotMember;
import stackpot.stackpot.pot.repository.PotMemberRepository;
import stackpot.stackpot.pot.repository.PotRepository;
import stackpot.stackpot.task.converter.TaskBoardConverter;
import stackpot.stackpot.task.dto.*;
import stackpot.stackpot.task.entity.Taskboard;
import stackpot.stackpot.task.entity.enums.TaskboardStatus;
import stackpot.stackpot.task.entity.mapping.Task;
import stackpot.stackpot.task.repository.TaskRepository;
import stackpot.stackpot.task.repository.TaskboardRepository;
import stackpot.stackpot.user.entity.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskQueryServiceImpl implements TaskQueryService {

    private final TaskBoardConverter taskboardConverter;
    private final TaskboardRepository taskboardRepository;
    private final TaskRepository taskRepository;
    private final PotRepository potRepository;
    private final PotMemberRepository potMemberRepository;
    private final AuthService authService;

    @Override
    public Map<TaskboardStatus, List<MyPotTaskPreViewResponseDto>> preViewTask(Long potId) {
        User user = authService.getCurrentUser();

        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        potMemberRepository.findByPotAndUser(pot, user)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND));

        List<Taskboard> taskboards = taskboardRepository.findByPot(pot);

        List<MyPotTaskPreViewResponseDto> taskboardDtos = taskboards.stream()
                .map(taskboard -> {
                    List<Task> tasks = taskRepository.findByTaskboard(taskboard);
                    List<PotMember> participants = tasks.stream()
                            .map(Task::getPotMember)
                            .distinct()
                            .collect(Collectors.toList());

                    return taskboardConverter.toDto(taskboard, participants);
                })
                .collect(Collectors.toList());


        return taskboardDtos.stream()
                .collect(Collectors.groupingBy(MyPotTaskPreViewResponseDto::getStatus));
    }


    @Override
    public MyPotTaskResponseDto viewDetailTask(Long potId, Long taskBoardId) {

        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        Taskboard taskboard = taskboardRepository.findByPotAndTaskboardId(pot, taskBoardId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.TASKBOARD_NOT_FOUND));

        List<Task> tasks = taskRepository.findByTaskboard(taskboard);

        List<PotMember> participants = tasks.stream()
                .map(Task::getPotMember)
                .distinct()
                .collect(Collectors.toList());

        MyPotTaskResponseDto response = taskboardConverter.toDTO(taskboard,participants);

        return response;
    }

    @Override
    public List<MyPotTaskPreViewResponseDto> getTasksFromDate(Long potId, LocalDate date) {
        User user = authService.getCurrentUser();

        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        potMemberRepository.findByPotAndUser(pot, user)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND));

        List<Taskboard> taskboards = taskboardRepository.findByPotAndDeadLine(pot,date);

        return taskboards.stream()
                .map(taskboard -> {
                    List<Task> tasks = taskRepository.findByTaskboard(taskboard); // Task 조회
                    List<PotMember> participants = tasks.stream()
                            .map(Task::getPotMember) // Task에서 PotMember 추출
                            .distinct()
                            .collect(Collectors.toList());

                    return taskboardConverter.toDto(taskboard, participants);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<MonthlyTaskDto> getMonthlyTasks(Long potId, int year, int month) {
        User user = authService.getCurrentUser();

        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        PotMember currentPotMember = potMemberRepository.findByPotAndUser(pot, user)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND));


        // 해당 월의 시작일과 마지막 일 계산
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 해당 월의 모든 Taskboard 조회
        List<Taskboard> taskboards = taskboardRepository
                .findByPotAndDeadLineBetweenOrderByDeadLineAsc(pot, startDate, endDate);

        return taskboards.stream()
                .map(taskboard -> {
                    List<Task> tasks = taskRepository.findByTaskboard(taskboard);
                    // 현재 사용자의 참여 여부 확인
                    boolean isParticipating = tasks.stream()
                            .map(Task::getPotMember)
                            .anyMatch(potMember -> potMember.equals(currentPotMember));

                    return MonthlyTaskDto.builder()
                            .taskId(taskboard.getTaskboardId())
                            .deadLine(taskboard.getDeadLine())
                            .isParticipating(isParticipating)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
