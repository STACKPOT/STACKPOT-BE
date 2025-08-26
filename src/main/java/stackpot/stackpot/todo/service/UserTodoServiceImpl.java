package stackpot.stackpot.todo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.PotHandler;
import stackpot.stackpot.badge.service.BadgeService;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.repository.MyPotRepository;
import stackpot.stackpot.pot.repository.PotRepository;
import stackpot.stackpot.todo.converter.UserTodoConverter;
import stackpot.stackpot.pot.entity.mapping.PotMember;
import stackpot.stackpot.badge.repository.PotMemberBadgeRepository;
import stackpot.stackpot.todo.dto.MyPotTodoResponseDTO;
import stackpot.stackpot.todo.dto.MyPotTodoUpdateRequestDTO;
import stackpot.stackpot.todo.entity.enums.TodoStatus;
import stackpot.stackpot.todo.entity.mapping.UserTodo;
import stackpot.stackpot.todo.repository.UserTodoRepository;
import stackpot.stackpot.pot.repository.PotMemberRepository;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.entity.enums.Role;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserTodoServiceImpl implements UserTodoService {

    private final UserTodoConverter userTodoConverter;
    private final PotMemberRepository potMemberRepository;
    private final MyPotRepository myPotRepository;
    private final PotRepository potRepository;
    private final AuthService authService;
    private static final Long DEFAULT_BADGE_ID = 1L;

    @Transactional
    @Override
    public Page<MyPotTodoResponseDTO> getTodo(Long potId, PageRequest pageRequest) {
        User current = authService.getCurrentUser();

        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        boolean isOwner = pot.getUser().equals(current);
        boolean isMember = potMemberRepository.existsByPotAndUser(pot, current);
        if (!isOwner && !isMember) throw new PotHandler(ErrorStatus.POT_FORBIDDEN);

        // PotMember 기준으로 전체 멤버 확보
        List<PotMember> allMembers = potMemberRepository.findByPotId(pot.getPotId());

        // 현재 사용자 우선 정렬
        allMembers.sort((m1, m2) -> {
            boolean a = m1.getUser().equals(current);
            boolean b = m2.getUser().equals(current);
            return a == b ? 0 : (a ? -1 : 1);
        });

        int total = allMembers.size();
        int start = (int) pageRequest.getOffset();
        int end = Math.min(start + pageRequest.getPageSize(), total);
        if (start >= total) return new PageImpl<>(List.of(), pageRequest, total);

        // 페이징된 PotMember
        List<PotMember> pageMembers = allMembers.subList(start, end);

        // 쿼리용 사용자 리스트
        List<User> pageUsers = pageMembers.stream()
                .map(PotMember::getUser)
                .collect(Collectors.toList());

        // 사용자별 역할(Role) 매핑 (멤버 역할)
        Map<User, Role> roleMap = pageMembers.stream()
                .collect(Collectors.toMap(PotMember::getUser,
                        pm -> pm.getRoleName() != null ? pm.getRoleName() : Role.UNKNOWN));

        // Todo 조회
        List<UserTodo> todos = myPotRepository.findByPotAndUsers(pot, pageUsers);

        // 5AM 컷 필터
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayAt5AM = LocalDateTime.of(now.toLocalDate(), LocalTime.of(5, 0));
        LocalDateTime yesterdayAt5AM = todayAt5AM.minusDays(1);

        List<UserTodo> filtered = todos.stream()
                .filter(t -> t.getCreatedAt().isAfter(yesterdayAt5AM))
                .collect(Collectors.toList());

        Map<User, List<UserTodo>> grouped = filtered.stream()
                .collect(Collectors.groupingBy(UserTodo::getUser));

        // Converter에 멤버 역할(Role) 전달
        List<MyPotTodoResponseDTO> response = pageMembers.stream()
                .map(pm -> userTodoConverter.toDto(
                        pm.getUser(),
                        roleMap.getOrDefault(pm.getUser(), Role.UNKNOWN),
                        pot,
                        grouped.getOrDefault(pm.getUser(), List.of()),
                        current))
                .collect(Collectors.toList());

        return new PageImpl<>(response, pageRequest, total);
    }


    @Transactional
    @Override
    public List<MyPotTodoResponseDTO> updateTodos(Long potId, List<MyPotTodoUpdateRequestDTO> reqs) {
        User current = authService.getCurrentUser();

        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        List<UserTodo> existing = myPotRepository.findByPot_PotIdAndUser(potId, current);
        Map<Long, UserTodo> existMap = existing.stream()
                .collect(Collectors.toMap(UserTodo::getTodoId, t -> t));

        Set<Long> requestedIds = reqs.stream()
                .map(MyPotTodoUpdateRequestDTO::getTodoId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<UserTodo> toSave = new ArrayList<>();
        for (MyPotTodoUpdateRequestDTO r : reqs) {
            if (r.getTodoId() != null && existMap.containsKey(r.getTodoId())) {
                UserTodo e = existMap.get(r.getTodoId());
                e.setContent(r.getContent());
                toSave.add(e);
            } else {
                toSave.add(UserTodo.builder()
                        .user(current).pot(pot)
                        .content(r.getContent())
                        .status(r.getStatus() != null ? r.getStatus() : TodoStatus.NOT_STARTED)
                        .build());
            }
        }
        List<UserTodo> toDelete = existing.stream()
                .filter(t -> !requestedIds.contains(t.getTodoId()))
                .collect(Collectors.toList());

        myPotRepository.saveAll(toSave);
        myPotRepository.deleteAll(toDelete);

        // ✅ current 사용자의 멤버 역할(Role) 구해 전달
        Role currentRole = potMemberRepository.findByPotId(potId).stream()
                .filter(pm -> pm.getUser().equals(current))
                .map(PotMember::getRoleName)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(Role.UNKNOWN);

        Map<User, List<UserTodo>> grouped = toSave.stream()
                .collect(Collectors.groupingBy(UserTodo::getUser));

        return grouped.entrySet().stream()
                .map(e -> userTodoConverter.toDto(
                        e.getKey(),
                        currentRole,    // ← 멤버 역할 전달
                        pot,
                        e.getValue(),
                        current))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public List<MyPotTodoResponseDTO> completeTodo(Long potId, Long todoId) {
        User current = authService.getCurrentUser();

        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        UserTodo todo = myPotRepository.findByTodoIdAndPot_PotId(todoId, potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.USER_TODO_NOT_FOUND));

        if (!todo.getUser().equals(current)) {
            throw new PotHandler(ErrorStatus.USER_TODO_UNAUTHORIZED);
        }

        todo.setStatus(todo.getStatus() == TodoStatus.COMPLETED
                ? TodoStatus.NOT_STARTED : TodoStatus.COMPLETED);
        myPotRepository.save(todo);

        List<UserTodo> all = myPotRepository.findByPot_PotId(potId);

        // ✅ User → Role 매핑 (멤버 역할)
        Map<User, Role> roleMap = potMemberRepository.findByPotId(potId).stream()
                .collect(Collectors.toMap(PotMember::getUser,
                        pm -> pm.getRoleName() != null ? pm.getRoleName() : Role.UNKNOWN));

        return userTodoConverter.toListDtoWithRoles(pot, all, roleMap, current);
    }

}

