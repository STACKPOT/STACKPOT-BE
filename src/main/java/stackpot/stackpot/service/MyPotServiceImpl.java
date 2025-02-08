package stackpot.stackpot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.MemberHandler;
import stackpot.stackpot.apiPayload.exception.handler.PotHandler;
import stackpot.stackpot.converter.MyPotConverter;
import stackpot.stackpot.converter.PotConverter;
import stackpot.stackpot.converter.PotDetailConverter;
import stackpot.stackpot.converter.TaskboardConverter;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.Taskboard;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.domain.enums.TaskboardStatus;
import stackpot.stackpot.domain.enums.TodoStatus;
import stackpot.stackpot.domain.mapping.PotMember;
import stackpot.stackpot.domain.mapping.Task;
import stackpot.stackpot.domain.mapping.UserTodo;
import stackpot.stackpot.repository.BadgeRepository.PotMemberBadgeRepository;
import stackpot.stackpot.repository.PotMemberRepository;
import stackpot.stackpot.repository.PotRepository.MyPotRepository;
import stackpot.stackpot.repository.PotRepository.PotRepository;
import stackpot.stackpot.repository.TaskRepository;
import stackpot.stackpot.repository.TaskboardRepository;
import stackpot.stackpot.repository.UserRepository.UserRepository;
import stackpot.stackpot.web.dto.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private final MyPotConverter myPotConverter;
    private final PotMemberBadgeRepository potMemberBadgeRepository;

    @Override
    public List<OngoingPotResponseDto> getMyPots() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // 내가 PotMember로 참여 중이고 상태가 'ONGOING'인 팟 조회 (내가 만든 팟 제외)
        List<Pot> ongoingMemberPots = potRepository.findByPotMembers_UserIdAndPotStatusOrderByCreatedAtDesc(user.getId(), "ONGOING");

        // DTO 변환 시 userId 추가
        return ongoingMemberPots.stream()
                .map(pot -> myPotConverter.convertToOngoingPotResponseDto(pot, user.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<OngoingPotResponseDto> getMyOngoingPots() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // 내가 생성한 ONGOING 상태의 팟 조회
        List<Pot> ongoingOwnedPots = potRepository.findByUserIdAndPotStatus(user.getId(), "ONGOING");

        // DTO 변환 시 userId 추가
        return ongoingOwnedPots.stream()
                .map(pot -> myPotConverter.convertToOngoingPotResponseDto(pot, user.getId()))
                .collect(Collectors.toList());
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
                .status(requestDTO.getStatus() != null ? requestDTO.getStatus() : TodoStatus.NOT_STARTED)
                .build();

        myPotRepository.save(userTodo);

        // 특정 팟의 모든 To-Do 조회 (업데이트된 리스트)
        List<UserTodo> potTodos = myPotRepository.findByPot_PotId(potId);

        //  투두를 User 기준으로 그룹핑
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

        //  팟의 모든 멤버 조회 (소유자 포함) 후, User 기준으로 페이징
        List<User> allPotMembers = potMemberRepository.findByPotId(pot.getPotId())
                .stream()
                .map(PotMember::getUser)
                .collect(Collectors.toList());

        allPotMembers.sort((u1, u2) -> u1.equals(user) ? -1 : (u2.equals(user) ? 1 : 0));

        //  User 기준으로 페이징 적용
        int totalUsers = allPotMembers.size();
        int startIndex = (int) pageRequest.getOffset();
        int endIndex = Math.min(startIndex + pageRequest.getPageSize(), totalUsers);

        if (startIndex >= totalUsers) {
            return new PageImpl<>(List.of(), pageRequest, totalUsers);
        }

        List<User> pagedUsers = allPotMembers.subList(startIndex, endIndex);

        //  선택된 User들의 투두 조회
        List<UserTodo> todos = myPotRepository.findByPotAndUsers(pot, pagedUsers);

        //  createdAt이 전날 새벽 3시 이후인 것만 필터링
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayAt3AM = LocalDateTime.of(now.toLocalDate(), LocalTime.of(3, 00));
        LocalDateTime yesterdayAt3AM = todayAt3AM.minusDays(1);

        List<UserTodo> filteredTodos = todos.stream()
                .filter(todo -> todo.getCreatedAt().isAfter(yesterdayAt3AM))
                .collect(Collectors.toList());

        //  투두를 User 기준으로 그룹핑
        Map<User, List<UserTodo>> groupedByUser = filteredTodos.stream()
                .collect(Collectors.groupingBy(UserTodo::getUser));

        // DTO 변환
        List<MyPotTodoResponseDTO> responseList = pagedUsers.stream()
                .map(member -> {
                    String roleName = getUserRoleInPot(member, pot);
                    List<UserTodo> userTodos = groupedByUser.getOrDefault(member, List.of());

                    return MyPotTodoResponseDTO.builder()
                            .userNickname(member.getNickname() + getVegetableNameByRole(roleName))
                            .userRole(roleName)
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

        //  Page 객체로 변환하여 반환
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
                // 기존 투두 수정 (status 변경 없이 유지)
                UserTodo existingTodo = existingTodoMap.get(request.getTodoId());

                if (!existingTodo.getUser().equals(user)) {
                    throw new PotHandler(ErrorStatus.USER_TODO_UNAUTHORIZED);
                }

                existingTodo.setContent(request.getContent());
                // 기존 상태 유지 (status 변경 없음)
                updatedOrNewTodos.add(existingTodo);
            } else {
                // 새로운 투두 생성
                UserTodo newTodo = UserTodo.builder()
                        .user(user)
                        .pot(pot)
                        .content(request.getContent())
                        .status(request.getStatus() != null ? request.getStatus() : TodoStatus.NOT_STARTED) // 기본값 설정
                        .build();
                updatedOrNewTodos.add(newTodo);
            }
        }

        // 삭제할 기존 투두 (삭제 요청이 없는 것만 남김)
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
                    List<UserTodo> userTodos = groupedByUser.getOrDefault(entry.getKey(), List.of());

                    return MyPotTodoResponseDTO.builder()
                            .userNickname(entry.getKey().getNickname() + getVegetableNameByRole(roleName))
                            .userId(entry.getKey().getId())
                            .todoCount(userTodos.size())
                            .todos(userTodos.stream()
                                    .map(todo -> MyPotTodoResponseDTO.TodoDetailDTO.builder()
                                            .todoId(todo.getTodoId())
                                            .content(todo.getContent())
                                            .status(todo.getStatus()) // 기존 상태 유지
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
        MyPotTaskResponseDto response = taskboardConverter.toDTO(taskboard, participants);
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
    public MyPotTaskResponseDto viewDetailTask(Long potId, Long taskboardId) {

        Pot pot = potRepository.findById(potId)
                .orElseThrow(()-> new IllegalArgumentException("pot을 찾을 수 없습니다."));

        Taskboard taskboard = taskboardRepository.findByPotAndTaskboardId(pot, taskboardId);
        if (taskboard == null) {
            throw new IllegalArgumentException("taskboard를 찾을 수 없습니다.");
        }

        List<Task> tasks = taskRepository.findByTaskboard(taskboard);

        List<PotMember> participants = tasks.stream()
                .map(Task::getPotMember) // Task에서 PotMember 추출
                .distinct()
                .collect(Collectors.toList());

        MyPotTaskResponseDto response = taskboardConverter.toDTO(taskboard,participants);

        return response;
    }


    @Override
    @Transactional
    public MyPotTaskResponseDto modfiyTask(Long potId, Long taskboardId, MyPotTaskRequestDto.create request){
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("pot을 찾을 수 없습니다."));

        Taskboard taskboard = taskboardRepository.findByPotAndTaskboardId(pot, taskboardId);
        if (taskboard == null) {
            throw new IllegalArgumentException("taskboard를 찾을 수 없습니다.");
        }

        updateUserData(taskboard, request);

        List<PotMember> participants = new ArrayList<>();

        log.info("참가자 {}", request.getParticipants());

        if (request.getParticipants() != null) {
            participants = potMemberRepository.findAllById(request.getParticipants());
            if (participants.isEmpty()) {
                throw new IllegalArgumentException("유효한 참가자를 찾을 수 없습니다. 요청된 ID를 확인해주세요.");
            }
            else{
                taskRepository.deleteByTaskboard(taskboard);
            }
        }
        else{
            List<Task> existingTasks = taskRepository.findByTaskboard(taskboard);
            for (Task task : existingTasks) {
                participants.add(task.getPotMember());
            }
        }

        createAndSaveTasks(taskboard, participants);
        List<MyPotTaskResponseDto.Participant> participantDtos = taskboardConverter.toParticipantDtoList(participants);

        MyPotTaskResponseDto response = taskboardConverter.toDTO(taskboard,participants);
        response.setParticipants(participantDtos);

        return response;
    }

    @Transactional
    @Override
    public void deleteTaskboard(Long potId, Long taskboardId) {
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("pot을 찾을 수 없습니다."));

        Taskboard taskboard = taskboardRepository.findByPotAndTaskboardId(pot, taskboardId);
        if (taskboard == null) {
            throw new IllegalArgumentException("taskboard를 찾을 수 없습니다.");
        }

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
        return pot.getPotMembers().stream()
                .filter(member -> member.getUser().equals(user))
                .map(member -> member.getRoleName().name())  // ENUM -> String 변환
                .findFirst()
                .orElse("UNKNOWN");  // 기본값 설정
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

        String userPotRole;

        // Pot 멤버의 Role 조회 후 변환
        userPotRole = potMemberRepository.findRoleByUserId(pot.getPotId(), user.getId())
                .map(role -> getKoreanRoleName(role.name())) //  Optional<Role>을 String으로 변환 후 한글 적용
                .orElse(getKoreanRoleName(pot.getUser().getRole().name())); // 기본값: Pot 생성자의 Role


        // DTO 반환
        return potDetailConverter.toCompletedPotDetailDto(pot, userPotRole, appealContent);
    }

    @Transactional
    @Override
    public List<CompletedPotBadgeResponseDto> getCompletedPotsWithBadges() {
        // 현재 인증된 사용자 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        //  사용자가 참여한 모든 COMPLETED 상태의 팟 조회 (뱃지 유무와 관계없이 가져옴)
        List<Pot> completedPots = potRepository.findCompletedPotsByUserId(user.getId());


        //  Pot -> DTO 변환
        return completedPots.stream()
                .map(pot -> {
                    //  역할별 참여자 수 조회 및 변환
                    List<Object[]> roleCounts = potMemberRepository.findRoleCountsByPotId(pot.getPotId());
                    Map<String, Integer> roleCountsMap = roleCounts.stream()
                            .collect(Collectors.toMap(
                                    roleCount -> ((Role) roleCount[0]).name(),
                                    roleCount -> ((Long) roleCount[1]).intValue()
                            ));

                    //  "프론트엔드(2), 백엔드(1)" 형식으로 변환
                    String formattedMembers = roleCountsMap.entrySet().stream()
                            .map(entry -> getKoreanRoleName(entry.getKey()) + "(" + entry.getValue() + ")")
                            .collect(Collectors.joining(", "));

                    Role userPotRole = potMemberRepository.findRoleByUserId(pot.getPotId(), user.getId())
                            .orElse(Role.FRONTEND);

                    //  사용자의 뱃지 조회 (뱃지가 없으면 빈 리스트 반환)
                    List<BadgeDto> myBadges = potMemberBadgeRepository.findByPotMember_Pot_PotIdAndPotMember_User_Id(pot.getPotId(), user.getId())
                            .stream()
                            .map(potMemberBadge -> new BadgeDto(
                                    potMemberBadge.getBadge().getBadgeId(),
                                    potMemberBadge.getBadge().getName()
                            ))
                            .collect(Collectors.toList());

                    //  Pot -> CompletedPotBadgeResponseDto 변환
                    return myPotConverter.toCompletedPotBadgeResponseDto(pot, formattedMembers, userPotRole, myBadges);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public List<CompletedPotBadgeResponseDto> getUserCompletedPotsWithBadges(Long userId) {

        //  사용자가 참여한 모든 COMPLETED 상태의 팟 조회 (뱃지 유무와 관계없이 가져옴)
        List<Pot> completedPots = potRepository.findCompletedPotsByUserId(userId);

        //  Pot -> DTO 변환
        return completedPots.stream()
                .map(pot -> {
                    //  역할별 참여자 수 조회 및 변환
                    List<Object[]> roleCounts = potMemberRepository.findRoleCountsByPotId(pot.getPotId());
                    Map<String, Integer> roleCountsMap = roleCounts.stream()
                            .collect(Collectors.toMap(
                                    roleCount -> ((Role) roleCount[0]).name(),
                                    roleCount -> ((Long) roleCount[1]).intValue()
                            ));

                    //  "프론트엔드(2), 백엔드(1)" 형식으로 변환
                    String formattedMembers = roleCountsMap.entrySet().stream()
                            .map(entry -> getKoreanRoleName(entry.getKey()) + "(" + entry.getValue() + ")")
                            .collect(Collectors.joining(", "));

                    Optional<Role> userPotRole = potMemberRepository.findRoleByUserId(pot.getPotId(), userId);

                    //  사용자의 뱃지 조회 (뱃지가 없으면 빈 리스트 반환)
                    List<BadgeDto> myBadges = potMemberBadgeRepository.findByPotMember_Pot_PotIdAndPotMember_User_Id(pot.getPotId(), userId)
                            .stream()
                            .map(potMemberBadge -> new BadgeDto(
                                    potMemberBadge.getBadge().getBadgeId(),
                                    potMemberBadge.getBadge().getName()
                            ))
                            .collect(Collectors.toList());

                    //  Pot -> CompletedPotBadgeResponseDto 변환
                    return myPotConverter.toCompletedPotBadgeResponseDto(pot, formattedMembers, userPotRole.orElse(null), myBadges);
                })
                .collect(Collectors.toList());
    }

    @Override
    public MyPotTaskStatusResponseDto updateTaskStatus(Long potId, Long taskId, TaskboardStatus status) {
        Taskboard taskboard = taskboardRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Taskboard not found with id: " + taskId));

         //Taskboard가 해당 Pot에 속해 있는지 확인
        if (!taskboard.getPot().getPotId().equals(potId)) {
            throw new IllegalArgumentException("The taskboard does not belong to the specified pot.");
        }

        // 입력받은 status 값으로 업데이트
        taskboard.setStatus(status);

        // 변경 사항 저장
        taskboardRepository.save(taskboard);

        return taskboardConverter.toTaskStatusDto(taskboard, status);
    }

    private String getKoreanRoleName(String role) {
        Map<String, String> roleToKoreanMap = Map.of(
                "BACKEND", "백엔드",
                "FRONTEND", "프론트엔드",
                "DESIGN", "디자인",
                "PLANNING", "기획"
        );
        return roleToKoreanMap.getOrDefault(role, "알 수 없음");
    }
}