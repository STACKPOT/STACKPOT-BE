package stackpot.stackpot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.MemberHandler;
import stackpot.stackpot.apiPayload.exception.handler.PotHandler;
import stackpot.stackpot.converter.PotConverter;
import stackpot.stackpot.converter.PotDetailConverter;
import stackpot.stackpot.converter.TaskboardConverter;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.Taskboard;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.TaskboardStatus;
import stackpot.stackpot.domain.enums.TodoStatus;
import stackpot.stackpot.domain.mapping.PotMember;
import stackpot.stackpot.domain.mapping.Task;
import stackpot.stackpot.domain.mapping.UserTodo;
import stackpot.stackpot.repository.PotMemberRepository;
import stackpot.stackpot.repository.PotRepository.MyPotRepository;
import stackpot.stackpot.repository.PotRepository.PotRepository;
import stackpot.stackpot.repository.TaskRepository;
import stackpot.stackpot.repository.TaskboardRepository;
import stackpot.stackpot.repository.UserRepository.UserRepository;
import stackpot.stackpot.web.dto.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyPotServiceImpl implements MyPotService {

    private final PotRepository potRepository;
    private final MyPotRepository myPotRepository;
    private final UserRepository userRepository;
    private final PotConverter potConverter;
    private final TaskboardConverter taskboardConverter;
    private final TaskboardRepository taskboardRepository;
    private final PotMemberRepository potMemberRepository;
    private final TaskRepository taskRepository;
    private final PotDetailConverter potDetailConverter;

    @Override
    public Map<String, List<MyPotResponseDTO.OngoingPotsDetail>> getMyOnGoingPots() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // 1. 내가 PotMember로 참여 중이고 상태가 'ONGOING'인 팟 조회
        List<Pot> ongoingMemberPots = potRepository.findByPotMembers_UserIdAndPotStatus(user.getId(), "ONGOING");

        // 2. 내가 만든 팟 중 상태가 'ONGOING'인 팟 조회
        List<Pot> ongoingOwnedPots = potRepository.findByUserIdAndPotStatus(user.getId(), "ONGOING");

        // Pot 리스트를 DTO로 변환
        List<MyPotResponseDTO.OngoingPotsDetail> memberPotsDetails = ongoingMemberPots.stream()
                .map(this::convertToOngoingPotDetail)
                .collect(Collectors.toList());

        List<MyPotResponseDTO.OngoingPotsDetail> ownedPotsDetails = ongoingOwnedPots.stream()
                .map(this::convertToOngoingPotDetail)
                .collect(Collectors.toList());

        // 결과를 분류하여 반환
        Map<String, List<MyPotResponseDTO.OngoingPotsDetail>> result = new HashMap<>();
        result.put("joinedOngoingPots", memberPotsDetails);
        result.put("ownedOngoingPots", ownedPotsDetails);

        return result;
    }


    @Override
    public List<MyPotTodoResponseDTO> postTodo(Long potId, MyPotTodoRequestDTO requestDTO) {
        // 현재 인증된 사용자 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 사용자 정보 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 팟 조회
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        // 소유자 또는 멤버 권한 확인
        boolean isOwner = pot.getUser().equals(user);
        boolean isMember = potMemberRepository.existsByPotAndUser(pot, user); // 팟의 멤버 여부 확인

        if (!isOwner && !isMember) {
            throw new PotHandler(ErrorStatus.POT_FORBIDDEN); // 권한 없음
        }

        // To-Do 생성
        UserTodo userTodo = UserTodo.builder()
                .pot(pot)
                .user(user)
                .content(requestDTO.getContent())
                .status(requestDTO.getStatus())
                .build();

        myPotRepository.save(userTodo);

        // 특정 팟의 모든 To-Do 조회 (업데이트된 리스트)
        List<UserTodo> potTodos = myPotRepository.findByPot_PotId(potId);

        // 📌 투두를 User 기준으로 그룹핑
        Map<User, List<UserTodo>> groupedByUser = potTodos.stream()
                .collect(Collectors.groupingBy(UserTodo::getUser));

        // 사용자별로 그룹화하여 반환
        return groupedByUser.entrySet().stream()
                .map(entry -> {
                    User currentUser = entry.getKey();
                    String roleName = getUserRoleInPot(currentUser, pot);
                    List<UserTodo> userTodos = groupedByUser.getOrDefault(currentUser, List.of());

                    return MyPotTodoResponseDTO.builder()
                            .userNickname(currentUser.getNickname() + getVegetableNameByRole(roleName))
                            .userId(currentUser.getId())
                            .todoCount(userTodos.size())
                            .todos(userTodos.stream()
                                    .map(todo -> MyPotTodoResponseDTO.TodoDetailDTO.builder()
                                            .todoId(todo.getTodoId())
                                            .content(todo.getContent())
                                            .status(todo.getStatus())
                                            .build())
                                    .collect(Collectors.toList()))
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Page<MyPotTodoResponseDTO> getTodo(Long potId, PageRequest pageRequest) {
        // 현재 인증된 사용자 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 사용자 정보 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 팟 조회
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        // 소유자 또는 멤버 권한 확인
        boolean isOwner = pot.getUser().equals(user);
        boolean isMember = potMemberRepository.existsByPotAndUser(pot, user);

        if (!isOwner && !isMember) {
            throw new PotHandler(ErrorStatus.POT_FORBIDDEN);
        }

        // 📌 팟의 모든 멤버 조회 (소유자 포함) 후, User 기준으로 페이징
        List<User> allPotMembers = potMemberRepository.findByPotId(pot.getPotId())
                .stream()
                .map(PotMember::getUser)
                .collect(Collectors.toList());
        allPotMembers.add(pot.getUser()); // 팟 소유자 추가

        allPotMembers.sort((u1, u2) -> u1.equals(user) ? -1 : (u2.equals(user) ? 1 : 0));

        // 📌 User 기준으로 페이징 적용
        int totalUsers = allPotMembers.size();
        int startIndex = (int) pageRequest.getOffset();
        int endIndex = Math.min(startIndex + pageRequest.getPageSize(), totalUsers);

        if (startIndex >= totalUsers) {
            return new PageImpl<>(List.of(), pageRequest, totalUsers);
        }

        List<User> pagedUsers = allPotMembers.subList(startIndex, endIndex);

        // 📌 선택된 User들의 투두 조회
        List<UserTodo> todos = myPotRepository.findByPotAndUsers(pot, pagedUsers);

        // 📌 투두를 User 기준으로 그룹핑
        Map<User, List<UserTodo>> groupedByUser = todos.stream()
                .collect(Collectors.groupingBy(UserTodo::getUser));

        // DTO 변환
        List<MyPotTodoResponseDTO> responseList = pagedUsers.stream()
                .map(member -> {
                    String roleName = getUserRoleInPot(member, pot);
                    List<UserTodo> userTodos = groupedByUser.getOrDefault(member, List.of());

                    return MyPotTodoResponseDTO.builder()
                            .userNickname(member.getNickname() + getVegetableNameByRole(roleName))
                            .userId(member.getId())
                            .todoCount(userTodos.size())
                            .todos(userTodos.isEmpty() ? null : userTodos.stream()
                                    .map(todo -> MyPotTodoResponseDTO.TodoDetailDTO.builder()
                                            .todoId(todo.getTodoId())
                                            .content(todo.getContent())
                                            .status(todo.getStatus())
                                            .build())
                                    .collect(Collectors.toList()))
                            .build();
                })
                .collect(Collectors.toList());

        // 📌 Page 객체로 변환하여 반환
        return new PageImpl<>(responseList, pageRequest, totalUsers);
    }


    @Override
    @Transactional
    public List<MyPotTodoResponseDTO> updateTodos(Long potId, List<MyPotTodoUpdateRequestDTO> requestList) {
        // 현재 인증된 사용자 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 사용자 정보 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 팟 조회
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        List<UserTodo> existingTodos = myPotRepository.findByPot_PotIdAndUser(potId, user);

        // 요청된 todoId 리스트
        Set<Long> requestedTodoIds = requestList.stream()
                .map(MyPotTodoUpdateRequestDTO::getTodoId)
                .filter(Objects::nonNull) // 새로 추가된 항목(null) 제외
                .collect(Collectors.toSet());

        Map<Long, UserTodo> existingTodoMap = existingTodos.stream()
                .collect(Collectors.toMap(UserTodo::getTodoId, todo -> todo));

        List<UserTodo> updatedOrNewTodos = new ArrayList<>();

        for (MyPotTodoUpdateRequestDTO request : requestList) {
            if (request.getTodoId() != null && existingTodoMap.containsKey(request.getTodoId())) {
                UserTodo existingTodo = existingTodoMap.get(request.getTodoId());

                if (!existingTodo.getUser().equals(user)) {
                    throw new PotHandler(ErrorStatus.USER_TODO_UNAUTHORIZED);
                }

                existingTodo.setContent(request.getContent());
                updatedOrNewTodos.add(existingTodo);
            } else {
                UserTodo newTodo = UserTodo.builder()
                        .user(user)
                        .pot(pot)
                        .content(request.getContent())
                        .status(request.getStatus() != null ? request.getStatus() : TodoStatus.NOT_STARTED) // 기본값 설정
                        .build();
                updatedOrNewTodos.add(newTodo);
            }
        }

        List<UserTodo> todosToDelete = existingTodos.stream()
                .filter(todo -> !requestedTodoIds.contains(todo.getTodoId()) && todo.getUser().equals(user)) // 본인만 삭제 가능
                .collect(Collectors.toList());

        myPotRepository.saveAll(updatedOrNewTodos);

        myPotRepository.deleteAll(todosToDelete);

        Map<User, List<UserTodo>> groupedByUser = updatedOrNewTodos.stream()
                .collect(Collectors.groupingBy(UserTodo::getUser));

        return groupedByUser.entrySet().stream()
                .map(entry -> {
                    // 해당 유저의 pot에서 potMember 역할 찾기
                    String roleName = getUserRoleInPot(entry.getKey(), pot);
                    List<UserTodo> userTodos = groupedByUser.getOrDefault(entry, List.of());

                    return MyPotTodoResponseDTO.builder()
                            .userNickname(entry.getKey().getNickname() + getVegetableNameByRole(roleName))
                            .userId(entry.getKey().getId())
                            .todoCount(userTodos.size())
                            .todos(entry.getValue().stream()
                                    .map(todo -> MyPotTodoResponseDTO.TodoDetailDTO.builder()
                                            .todoId(todo.getTodoId())
                                            .content(todo.getContent())
                                            .status(todo.getStatus())
                                            .build())
                                    .collect(Collectors.toList()))
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public List<MyPotTodoResponseDTO> completeTodo(Long potId, Long todoId) {
        // 현재 로그인한 사용자 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 사용자 정보 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 팟 조회
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        // 해당 투두가 존재하는지 확인 및 소유자 검증
        UserTodo userTodo = myPotRepository.findByTodoIdAndPot_PotId(todoId, potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.USER_TODO_NOT_FOUND));

        if (!userTodo.getUser().equals(user)) {
            throw new PotHandler(ErrorStatus.USER_TODO_UNAUTHORIZED);
        }

        // To-Do 상태 업데이트
        userTodo.setStatus(TodoStatus.COMPLETED);
        myPotRepository.save(userTodo);

        // 특정 팟의 모든 To-Do 조회 후 반환
        List<UserTodo> potTodos = myPotRepository.findByPot_PotId(potId);

        Map<User, List<UserTodo>> groupedByUser = potTodos.stream()
                .collect(Collectors.groupingBy(UserTodo::getUser));

        return potTodos.stream()
                .collect(Collectors.groupingBy(UserTodo::getUser))
                .entrySet().stream()
                .map(entry -> {
                    // 소유자인지 확인하고 적절한 역할 적용
                    String roleName = getUserRoleInPot(entry.getKey(), pot);
                    String userNicknameWithRole = entry.getKey().getNickname() + getVegetableNameByRole(roleName);
                    List<UserTodo> userTodos = groupedByUser.getOrDefault(entry, List.of());

                    return MyPotTodoResponseDTO.builder()
                            .userNickname(userNicknameWithRole)
                            .userId(entry.getKey().getId())
                            .todoCount(userTodos.size())
                            .todos(entry.getValue().stream()
                                    .map(todo -> MyPotTodoResponseDTO.TodoDetailDTO.builder()
                                            .todoId(todo.getTodoId())
                                            .content(todo.getContent())
                                            .status(todo.getStatus())
                                            .build())
                                    .collect(Collectors.toList()))
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public MyPotTaskResponseDto creatTask(Long potId, MyPotTaskRequestDto.create request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("Pot not found with id: " + potId));

        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new IllegalArgumentException("User not found with email: " + email));

        Taskboard taskboard = taskboardConverter.toTaskboard(pot, request);
        taskboard.setUser(user);
        taskboardRepository.save(taskboard);

        List<Long> requestedParticipantIds = request.getParticipants();

        List<PotMember> validParticipants = potMemberRepository.findByPotId(potId);

        List<PotMember> participants = validParticipants.stream()
                .filter(potMember -> requestedParticipantIds.contains(potMember.getPotMemberId()))
                .collect(Collectors.toList());

        log.info("유효한 참가자 목록: {}", participants);

        if (participants.isEmpty()) {
            throw new IllegalArgumentException("유효한 참가자를 찾을 수 없습니다. 요청된 ID를 확인해주세요.");
        }
        createAndSaveTasks(taskboard, participants);

        List<MyPotTaskResponseDto.Participant> participantDtos = taskboardConverter.toParticipantDtoList(participants);
        MyPotTaskResponseDto response = taskboardConverter.toDTO(taskboard);
        response.setParticipants(participantDtos);

        return response;
    }

    @Override
    public Map<TaskboardStatus, List<MyPotTaskPreViewResponseDto>> preViewTask(Long potId) {

        Pot pot = potRepository.findById(potId)
                .orElseThrow(()->new IllegalArgumentException("Pot not found with id: " + potId));

        List<Taskboard> taskboards = taskboardRepository.findByPot(pot);

        List<MyPotTaskPreViewResponseDto> taskboardDtos = taskboards.stream()
                .map(taskboard -> {
                    List<Task> tasks = taskRepository.findByTaskboard(taskboard); // Task 조회
                    List<PotMember> participants = tasks.stream()
                            .map(Task::getPotMember) // Task에서 PotMember 추출
                            .distinct()
                            .collect(Collectors.toList());

                    return taskboardConverter.toDto(taskboard, participants);
                })
                .collect(Collectors.toList());

        return taskboardDtos.stream()
                .collect(Collectors.groupingBy(MyPotTaskPreViewResponseDto::getStatus));
    }


    @Override
    public MyPotTaskResponseDto viewDetailTask(Long taskboardId) {

        Taskboard taskboard = taskboardRepository.findById(taskboardId)
                .orElseThrow(() -> new IllegalArgumentException("Taskboard not found with id: " + taskboardId));

        MyPotTaskResponseDto response = taskboardConverter.toDTO(taskboard);

        return response;
    }

    private MyPotResponseDTO.OngoingPotsDetail convertToOngoingPotDetail(Pot pot) {
        List<PotMemberResponseDTO> potMembers = pot.getPotMembers().stream()
                .map(member -> PotMemberResponseDTO.builder()
                        .potMemberId(member.getPotMemberId())
                        .roleName(member.getRoleName())
                        .appealContent(member.getAppealContent())
                        .build())
                .collect(Collectors.toList());

        return MyPotResponseDTO.OngoingPotsDetail.builder()
                .user(UserResponseDto.Userdto.builder()
                        .nickname(pot.getUser().getNickname())
                        .role(pot.getUser().getRole())
                        .build())
                .pot(potConverter.toDto(pot, pot.getRecruitmentDetails()))
                .potMembers(potMembers)
                .build();
    }


    @Override
    public MyPotTaskResponseDto modfiyTask(Long taskId, MyPotTaskRequestDto.create request) {

        Taskboard taskboard = taskboardRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Taskboard not found with id: " + taskId));

        updateUserData(taskboard, request);

        List<PotMember> participants = new ArrayList<>();
        if (request.getParticipants() != null && !request.getParticipants().isEmpty()) {
            participants = potMemberRepository.findAllById(request.getParticipants());
            if (participants.isEmpty()) {
                throw new IllegalArgumentException("유효한 참가자를 찾을 수 없습니다. 요청된 ID를 확인해주세요.");
            }
        }
        createAndSaveTasks(taskboard, participants);
        List<MyPotTaskResponseDto.Participant> participantDtos = taskboardConverter.toParticipantDtoList(participants);

        MyPotTaskResponseDto response = taskboardConverter.toDTO(taskboard);
        response.setParticipants(participantDtos);

        return response;
    }

    @Transactional
    @Override
    public void deleteTaskboard(Long potId, Long taskboardId) {
        Taskboard taskboard = taskboardRepository.findById(taskboardId)
                .orElseThrow(() -> new IllegalArgumentException("Taskboard not found with id: " + taskboardId));

//        // Taskboard가 해당 Pot에 속해 있는지 확인
//        if (!taskboard.getPot().getId().equals(potId)) {
//            throw new IllegalArgumentException("The taskboard does not belong to the specified pot.");
//        }

        // Taskboard에 연결된 Task 삭제
        List<Task> tasks = taskRepository.findByTaskboard(taskboard);
        taskRepository.deleteAll(tasks);

        // Taskboard 삭제
        taskboardRepository.delete(taskboard);
    }

    // 역할에 따른 채소명을 반환하는 메서드
    private String getVegetableNameByRole(String role) {
        Map<String, String> roleToVegetableMap = Map.of(
                "BACKEND", " 양파",
                "FRONTEND", " 버섯",
                "DESIGN", " 브로콜리",
                "PLANNING", " 당근"
        );
        return roleToVegetableMap.getOrDefault(role, "알 수 없음");
    }


    private String getUserRoleInPot(User user, Pot pot) {
        if (pot.getUser().equals(user)) {
            // 소유자인 경우, 사용자의 역할을 직접 가져옴
            return pot.getUser().getRole().name();
        } else {
            // 참여자인 경우, potMember에서 역할을 가져옴
            return pot.getPotMembers().stream()
                    .filter(member -> member.getUser().equals(user))
                    .map(member -> member.getRoleName().name())  // ENUM -> String 변환
                    .findFirst()
                    .orElse("UNKNOWN");  // 기본값 설정
        }
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

    private List<Task> createAndSaveTasks(Taskboard taskboard, List<PotMember> participants) {
        List<Task> tasks = participants.stream()
                .map(participant -> Task.builder()
                        .taskboard(taskboard)
                        .potMember(participant)
                        .build())
                .collect(Collectors.toList());

        return taskRepository.saveAll(tasks);
    }

    @Transactional
    @Override
    public CompletedPotDetailResponseDto getCompletedPotDetail(Long potId) {
        // 현재 로그인한 사용자 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 팟 조회
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팟을 찾을 수 없습니다."));

        // 팟 상태 확인
        if (!"COMPLETED".equals(pot.getPotStatus())) {
            throw new IllegalArgumentException("해당 팟은 COMPLETED 상태가 아닙니다.");
        }

        // 팟 멤버에서 어필 내용 가져오기
        PotMember potMember = potMemberRepository.findByPotAndUser(pot, user)
                .orElse(null);

        String appealContent = (potMember != null) ? potMember.getAppealContent() : null;

        // DTO 반환
        return potDetailConverter.toCompletedPotDetailDto(pot, appealContent);
    }
}