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
public class TaskServiceImpl implements TaskService {

    private final TaskBoardConverter taskboardConverter;
    private final TaskboardRepository taskboardRepository;
    private final TaskRepository taskRepository;
    private final PotRepository potRepository;
    private final PotMemberRepository potMemberRepository;
    private final AuthService authService;

    @Override
    public MyPotTaskResponseDto creatTask(Long potId, MyPotTaskRequestDto.create request) {
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        User user = authService.getCurrentUser();

        potMemberRepository.findByPotAndUser(pot, user)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND));

        Taskboard taskboard = taskboardConverter.toTaskboard(pot, request);
        taskboard.setUser(user);
        taskboardRepository.save(taskboard);

        List<Long> requestedParticipantIds = request.getParticipants() != null ? request.getParticipants() : List.of();


        List<PotMember> validParticipants = potMemberRepository.findByPotId(potId);

        List<PotMember> participants = validParticipants.stream()
                .filter(potMember -> requestedParticipantIds.contains(potMember.getPotMemberId()))
                .collect(Collectors.toList());

        createAndSaveTasks(taskboard, participants);

        List<MyPotTaskResponseDto.Participant> participantDtos = taskboardConverter.toParticipantDtoList(participants);
        MyPotTaskResponseDto response = taskboardConverter.toDTO(taskboard, participants);
        response.setParticipants(participantDtos);

        return response;
    }

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
    @Transactional
    public MyPotTaskResponseDto modifyTask(Long potId, Long taskBoardId, MyPotTaskRequestDto.create request) {
        User user = authService.getCurrentUser();

        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        potMemberRepository.findByPotAndUser(pot, user)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND));

        Taskboard taskboard = taskboardRepository.findByPotAndTaskboardId(pot, taskBoardId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.TASKBOARD_NOT_FOUND));

        updateUserData(taskboard, request);

        List<Long> requestedParticipantIds = request.getParticipants() != null ? request.getParticipants() : List.of();
        List<PotMember> participants = potMemberRepository.findAllById(requestedParticipantIds);

        taskRepository.deleteByTaskboard(taskboard);

        if (!participants.isEmpty()) createAndSaveTasks(taskboard, participants);

        List<MyPotTaskResponseDto.Participant> participantDtos = taskboardConverter.toParticipantDtoList(participants);
        MyPotTaskResponseDto response = taskboardConverter.toDTO(taskboard, participants);
        response.setParticipants(participantDtos);

        return response;
    }

    @Transactional
    @Override
    public void deleteTaskBoard(Long potId, Long taskBoardId) {
        User user = authService.getCurrentUser();

        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        potMemberRepository.findByPotAndUser(pot, user)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND));

        Taskboard taskboard = taskboardRepository.findByPotAndTaskboardId(pot, taskBoardId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.TASKBOARD_NOT_FOUND));

        // Taskboard에 연결된 Task 삭제
        List<Task> tasks = taskRepository.findByTaskboard(taskboard);
        taskRepository.deleteAll(tasks);

        // Taskboard 삭제
        taskboardRepository.delete(taskboard);
    }

    private List<Task> createAndSaveTasks(Taskboard taskboard, List<PotMember> participants) {
        List<Task> tasks = participants.stream()
                .map(participant -> Task.builder()
                        .taskboard(taskboard)
                        .potMember(participant)
                        .build())
                .collect(Collectors.toList());

        return taskRepository.saveAll(tasks);
    }

    @Override
    public MyPotTaskStatusResponseDto updateTaskStatus(Long potId, Long taskId, TaskboardStatus status) {
        User user = authService.getCurrentUser();

        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        potMemberRepository.findByPotAndUser(pot, user)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND));

        Taskboard taskboard = taskboardRepository.findById(taskId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.TASKBOARD_NOT_FOUND));

        if (!taskboard.getPot().getPotId().equals(potId)) {
            throw new PotHandler(ErrorStatus.TASKBOARD_POT_MISMATCH);
        }

        // 입력받은 status 값으로 업데이트
        taskboard.setStatus(status);

        // 변경 사항 저장
        taskboardRepository.save(taskboard);

        return taskboardConverter.toTaskStatusDto(taskboard, status);
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

    private void updateUserData(Taskboard taskboard, MyPotTaskRequestDto.create request) {
        if(request.getTitle() !=null){
            taskboard.setTitle(request.getTitle());
        }
        if(request.getDescription()!=null){
            taskboard.setDescription(request.getDescription());
        }
        if(request.getDeadline()!=null){
            taskboard.setDeadLine(request.getDeadline());
        }
        if(request.getTaskboardStatus()!=null){
            taskboard.setStatus(request.getTaskboardStatus());
        }
    }
}
