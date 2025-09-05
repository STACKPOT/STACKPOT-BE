package stackpot.stackpot.task.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import stackpot.stackpot.user.entity.enums.Role;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        potMemberRepository.findByPotPotIdAndUser(potId, user)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND));
        List<Taskboard> taskboards = taskboardRepository.findByPotPotId(potId);
        List<MyPotTaskPreViewResponseDto> taskboardDtos = taskboards.stream()
                .map(taskboard -> {
                    List<Task> tasks = taskRepository.findByTaskboard(taskboard);
                    List<PotMember> participants = tasks.stream()
                            .map(Task::getPotMember)
                            .distinct()
                            .collect(Collectors.toList());
                    Role creatorRole = potMemberRepository
                            .findRoleByUserId(taskboard.getPot().getPotId(), taskboard.getUser().getUserId())
                            .orElse(Role.UNKNOWN);
                    return taskboardConverter.toDto(taskboard, participants,creatorRole );
                })
                .toList();

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
        Role creatorRole = potMemberRepository
                .findRoleByUserId(taskboard.getPot().getPotId(), taskboard.getUser().getUserId())
                .orElse(Role.UNKNOWN);
        return taskboardConverter.toDTO(taskboard, participants,creatorRole);
    }

//    @Override
//    public List<MyPotTaskPreViewResponseDto> getTasksFromDate(Long potId, LocalDate date) {
//        User user = authService.getCurrentUser();
//
//        potMemberRepository.findByPotPotIdAndUser(potId, user)
//                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND));
//
//        List<Taskboard> taskboards = taskboardRepository.findByPotPotIdAndDeadLine(potId, date);
//
//        return taskboards.stream()
//                .map(taskboard -> {
//                    List<Task> tasks = taskRepository.findByTaskboard(taskboard); // Task 조회
//                    List<PotMember> participants = tasks.stream()
//                            .map(Task::getPotMember) // Task에서 PotMember 추출
//                            .distinct()
//                            .collect(Collectors.toList());
//                    Role creatorRole = potMemberRepository
//                            .findRoleByUserId(taskboard.getPot().getPotId(), taskboard.getUser().getUserId())
//                            .orElse(Role.UNKNOWN);
//                    return taskboardConverter.toDto(taskboard, participants,creatorRole);
//                })
//                .collect(Collectors.toList());
//    }
    @Override
    @Transactional
    public List<MyPotTaskPreViewResponseDto> getTasksFromDate(Long potId, LocalDate date) {
        User me = authService.getCurrentUser();

        // 1) 멤버십 체크
        potMemberRepository.findByPotPotIdAndUser(potId, me)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND));

        // 2) Taskboard 배치 조회
        List<Taskboard> boards = taskboardRepository.findByPotPotIdAndDeadLine(potId, date);
        if (boards.isEmpty()) return List.of();

        // 3) Task 배치 조회 후 그룹핑
        List<Task> allTasks = taskRepository.findByTaskboardIn(boards);
        Map<Long, List<Task>> tasksByBoardId = allTasks.stream()
                .collect(Collectors.groupingBy(t -> t.getTaskboard().getTaskboardId()));

        // 4) 작성자 역할 배치 조회
        Set<Long> creatorIds = boards.stream()
                .map(tb -> tb.getUser().getUserId())
                .collect(Collectors.toSet());

        Map<Long, Role> creatorRoleMap = potMemberRepository
                .findCreatorRolesByPotAndUserIds(potId, creatorIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Role) row[1],
                        (a, b) -> a
                ));

        // 5) DTO 변환
        return boards.stream().map(tb -> {
            // participants: Task → PotMember → id distinct
            List<Task> tasks = tasksByBoardId.getOrDefault(tb.getTaskboardId(), List.of());
            List<PotMember> participants = tasks.stream()
                    .map(Task::getPotMember)
                    .collect(Collectors.toMap(PotMember::getPotMemberId, pm -> pm, (a, b) -> a))
                    .values().stream().toList();

            Role creatorRole = creatorRoleMap.getOrDefault(tb.getUser().getUserId(), Role.UNKNOWN);

            return taskboardConverter.toDto(tb, participants, creatorRole);
        }).toList();
    }


    @Override
    public List<MonthlyTaskDto> getMonthlyTasks(Long potId, int year, int month) {
        User user = authService.getCurrentUser();

        PotMember currentPotMember = potMemberRepository.findByPotPotIdAndUser(potId, user)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND));

        // 해당 월의 시작일과 마지막 일 계산
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 해당 월의 모든 Taskboard 조회
        List<Taskboard> taskboards = taskboardRepository
                .findByPotPIdAndDeadLineBetweenOrderByDeadLineAsc(potId, startDate, endDate);

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

    @Override
    public List<PotMember> getTop2TaskCountByPotMemberId(List<Long> potMemberIds) {
        Pageable pageable = PageRequest.of(0, 2);
        return taskRepository.getTop2TaskCountByPotMemberId(potMemberIds, pageable);
    }
}
