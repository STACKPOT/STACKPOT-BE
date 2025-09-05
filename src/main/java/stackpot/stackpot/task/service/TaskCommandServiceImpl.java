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
import stackpot.stackpot.user.entity.enums.Role;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskCommandServiceImpl implements TaskCommandService {

    private final TaskBoardConverter taskboardConverter;
    private final TaskboardRepository taskboardRepository;
    private final TaskRepository taskRepository;
    private final PotRepository potRepository;
    private final PotMemberRepository potMemberRepository;
    private final AuthService authService;

    @Override
    public MyPotTaskResponseDto createTask(Long potId, MyPotTaskRequestDto.create request) {
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        User user = authService.getCurrentUser();

        potMemberRepository.findByPotAndUser(pot, user)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND));

        Taskboard taskboard = taskboardConverter.toTaskboard(pot, request, user);
        taskboardRepository.save(taskboard);

        List<Long> requestedParticipantIds = request.getParticipants() != null ? request.getParticipants() : List.of();
        List<PotMember> validParticipants = potMemberRepository.findByPotId(potId);
        List<PotMember> participants = validParticipants.stream()
                .filter(potMember -> requestedParticipantIds.contains(potMember.getPotMemberId()))
                .collect(Collectors.toList());

        this.createAndSaveTasks(taskboard, participants);

        List<MyPotTaskResponseDto.Participant> participantDtos = taskboardConverter.toParticipantDtoList(participants);
        Role creatorRole = potMemberRepository
                .findRoleByUserId(taskboard.getPot().getPotId(), taskboard.getUser().getUserId())
                .orElse(Role.UNKNOWN);

        MyPotTaskResponseDto response = taskboardConverter.toDTO(taskboard, participants,creatorRole);
        response.setParticipants(participantDtos);

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

        Role creatorRole = potMemberRepository
                .findRoleByUserId(taskboard.getPot().getPotId(), taskboard.getUser().getUserId())
                .orElse(Role.UNKNOWN);

        MyPotTaskResponseDto response = taskboardConverter.toDTO(taskboard, participants,creatorRole);
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

    private void createAndSaveTasks(Taskboard taskboard, List<PotMember> participants) {
        List<Task> tasks = participants.stream()
                .map(participant -> Task.builder()
                        .taskboard(taskboard)
                        .potMember(participant)
                        .build())
                .collect(Collectors.toList());
        taskRepository.saveAll(tasks);
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
