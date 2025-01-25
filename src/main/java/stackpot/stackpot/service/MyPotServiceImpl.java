package stackpot.stackpot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import stackpot.stackpot.converter.PotConverter;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.domain.mapping.UserTodo;
import stackpot.stackpot.repository.PotRepository.MyPotRepository;
import stackpot.stackpot.repository.PotRepository.PotRepository;
import stackpot.stackpot.repository.UserRepository.UserRepository;
import stackpot.stackpot.web.dto.*;

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
    public List<MyPotResponseDTO> getMyOnGoingPots() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // 사용자가 만든 팟 조회
        List<Pot> myPots = potRepository.findByUserId(user.getId());

        // 진행 중인 팟 리스트 변환 (멤버 정보 포함)
        List<MyPotResponseDTO.OngoingPotsDetail> ongoingPots = myPots.stream()
                .filter(pot -> "recruiting".equals(pot.getPotStatus()))
                .map(this::convertToOngoingPotDetail)
                .collect(Collectors.toList());

        // MyPotResponseDTO로 변환하여 반환
        return List.of(MyPotResponseDTO.builder()
                .ongoingPots(ongoingPots)
                .build());
    }


    @Override
    public List<MyPotTodoResponseDTO> postTodo(Long potId, MyPotTodoRequestDTO requestDTO) {
        // 현재 인증된 사용자 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // 해당 Pot 존재 여부 확인
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("Pot not found with id: " + potId));


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
                    String roleName = entry.getValue().stream()
                            .findFirst()
                            .flatMap(todo -> todo.getPot().getPotMembers().stream()
                                    .filter(member -> member.getUser().equals(entry.getKey()))
                                    .map(member -> member.getRoleName().name())  // ENUM -> String 변환
                                    .findFirst()
                            )
                            .orElse("UNKNOWN");  // 기본값 설정

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

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // 해당 Pot 존재 여부 확인
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("Pot not found with id: " + potId));

        // 특정 팟의 모든 To-Do 조회
        List<UserTodo> potTodos = myPotRepository.findByPot_PotId(potId);

        // 특정 팟의 모든 To-Do 조회
        return potTodos.stream()
                .collect(Collectors.groupingBy(UserTodo::getUser))
                .entrySet().stream()
                .map(entry -> {
                    // 해당 유저의 pot에서 potMember 역할 찾기
                    String roleName = pot.getPotMembers().stream()
                            .filter(member -> member.getUser().equals(entry.getKey()))
                            .map(member -> member.getRoleName().name())  // Enum을 String으로 변환
                            .findFirst()
                            .orElse("UNKNOWN");  // 기본값을 String으로 설정

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

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // 해당 Pot 존재 여부 확인
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("Pot not found with id: " + potId));

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
                    String roleName = entry.getValue().stream()
                            .findFirst()
                            .flatMap(todo -> todo.getPot().getPotMembers().stream()
                                    .filter(member -> member.getUser().equals(entry.getKey()))
                                    .map(member -> member.getRoleName().name())  // ENUM -> String 변환
                                    .findFirst()
                            )
                            .orElse("UNKNOWN");  // 기본값 설정

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

    // 진행 중인 팟 변환 메서드 (멤버 포함)
    private MyPotResponseDTO.OngoingPotsDetail convertToOngoingPotDetail(Pot pot) {
        List<PotRecruitmentResponseDto> recruitmentDetails = pot.getRecruitmentDetails().stream()
                .map(details -> PotRecruitmentResponseDto.builder()
                        .recruitmentId(details.getRecruitmentId())
                        .recruitmentRole(details.getRecruitmentRole().name())
                        .recruitmentCount(details.getRecruitmentCount())
                        .build())
                .collect(Collectors.toList());

        List<PotMemberResponseDTO> potMembers = pot.getPotMembers().stream()
                .map(member -> PotMemberResponseDTO.builder()
                        .potMemberId(member.getPotMemberId())
                        .roleName(member.getRoleName())

                        .build())
                .collect(Collectors.toList());

        return MyPotResponseDTO.OngoingPotsDetail.builder()
                .user(UserResponseDto.builder()
                        .nickname(pot.getUser().getNickname() + getVegetableNameByRole(String.valueOf(pot.getUser().getRole())))
                        .role(pot.getUser().getRole())
                        .build())
                .pot(potConverter.toDto(pot, pot.getRecruitmentDetails()))  // 변환기 사용
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

}