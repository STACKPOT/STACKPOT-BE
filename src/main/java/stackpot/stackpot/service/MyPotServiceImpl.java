package stackpot.stackpot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.MemberHandler;
import stackpot.stackpot.apiPayload.exception.handler.PotHandler;
import stackpot.stackpot.converter.PotConverter;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.TodoStatus;
import stackpot.stackpot.domain.mapping.UserTodo;
import stackpot.stackpot.repository.PotRepository.MyPotRepository;
import stackpot.stackpot.repository.PotRepository.PotRepository;
import stackpot.stackpot.repository.UserRepository.UserRepository;
import stackpot.stackpot.web.dto.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyPotServiceImpl implements MyPotService {

    private final PotRepository potRepository;
    private final MyPotRepository myPotRepository;
    private final UserRepository userRepository;
    private final PotConverter potConverter;


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

        // 소유자 확인
        if (!pot.getUser().equals(user)) {
            throw new PotHandler(ErrorStatus.POT_FORBIDDEN);
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

        // 사용자별로 그룹화하여 반환
        return potTodos.stream()
                .collect(Collectors.groupingBy(UserTodo::getUser))
                .entrySet().stream()
                .map(entry -> {
                    // 해당 유저의 pot에서 potMember 역할 찾기
                    String roleName = getUserRoleInPot(entry.getKey(), pot);

                    return MyPotTodoResponseDTO.builder()
                            .userNickname(entry.getKey().getNickname() + getVegetableNameByRole(roleName))
                            .userId(entry.getKey().getId())
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
    public List<MyPotTodoResponseDTO> getTodo(Long potId) {
        // 현재 인증된 사용자 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 사용자 정보 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 팟 조회
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        // 소유자 확인
        if (!pot.getUser().equals(user)) {
            throw new PotHandler(ErrorStatus.POT_FORBIDDEN);
        }

        // 특정 팟의 모든 To-Do 조회
        List<UserTodo> potTodos = myPotRepository.findByPot_PotId(potId);

        // 특정 팟의 모든 To-Do 조회
        return potTodos.stream()
                .collect(Collectors.groupingBy(UserTodo::getUser))
                .entrySet().stream()
                .map(entry -> {
                    // 해당 유저의 pot에서 potMember 역할 찾기
                    String roleName = getUserRoleInPot(entry.getKey(), pot); // 기본값을 String으로 설정

                    return MyPotTodoResponseDTO.builder()
                            .userNickname(entry.getKey().getNickname() + getVegetableNameByRole(roleName))
                            .userId(entry.getKey().getId())
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

        // 소유자 확인
        if (!pot.getUser().equals(user)) {
            throw new PotHandler(ErrorStatus.POT_FORBIDDEN);
        }

        // 특정 팟에 속한 모든 투두 리스트 조회 (사용자별)
        List<UserTodo> userTodos = myPotRepository.findByPotAndUser(pot, user);

        // 요청된 todoId와 일치하는 항목 업데이트
        Map<Long, UserTodo> todoMap = userTodos.stream()
                .collect(Collectors.toMap(UserTodo::getTodoId, todo -> todo));

        for (MyPotTodoUpdateRequestDTO updateRequest : requestList) {
            UserTodo todo = todoMap.get(updateRequest.getTodoId());
            if (todo == null) {
                throw new IllegalArgumentException("Todo with ID " + updateRequest.getTodoId() + " not found.");
            }

            // 내용 업데이트
            todo.setContent(updateRequest.getContent());
        }

        // 변경된 상태 저장
        myPotRepository.saveAll(userTodos);

        // 사용자별로 그룹화하여 DTO로 변환
        Map<User, List<UserTodo>> groupedByUser = userTodos.stream()
                .collect(Collectors.groupingBy(UserTodo::getUser));

        return groupedByUser.entrySet().stream()
                .map(entry -> {
                    // 해당 유저의 pot에서 potMember 역할 찾기
                    String roleName = getUserRoleInPot(entry.getKey(), pot);

                    return MyPotTodoResponseDTO.builder()
                            .userNickname(entry.getKey().getNickname() + getVegetableNameByRole(roleName))
                            .userId(entry.getKey().getId())
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

        return potTodos.stream()
                .collect(Collectors.groupingBy(UserTodo::getUser))
                .entrySet().stream()
                .map(entry -> {
                    // 소유자인지 확인하고 적절한 역할 적용
                    String roleName = getUserRoleInPot(entry.getKey(), pot);
                    String userNicknameWithRole = entry.getKey().getNickname() + getVegetableNameByRole(roleName);

                    return MyPotTodoResponseDTO.builder()
                            .userNickname(userNicknameWithRole)
                            .userId(entry.getKey().getId())
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

    private MyPotResponseDTO.OngoingPotsDetail convertToOngoingPotDetail(Pot pot) {
        List<PotMemberResponseDTO> potMembers = pot.getPotMembers().stream()
                .map(member -> PotMemberResponseDTO.builder()
                        .potMemberId(member.getPotMemberId())
                        .roleName(member.getRoleName())
                        .appealContent(member.getAppealContent())
                        .build())
                .collect(Collectors.toList());

        return MyPotResponseDTO.OngoingPotsDetail.builder()
                .user(UserResponseDto.builder()
                        .nickname(pot.getUser().getNickname())
                        .role(pot.getUser().getRole())
                        .build())
                .pot(potConverter.toDto(pot, pot.getRecruitmentDetails()))
                .potMembers(potMembers)
                .build();
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


}