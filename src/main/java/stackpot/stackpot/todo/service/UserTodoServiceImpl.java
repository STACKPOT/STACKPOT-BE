package stackpot.stackpot.todo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.MemberHandler;
import stackpot.stackpot.apiPayload.exception.handler.PotHandler;
import stackpot.stackpot.badge.service.BadgeService;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.repository.MyPotRepository;
import stackpot.stackpot.pot.repository.PotRepository;
import stackpot.stackpot.todo.converter.UserTodoConverter;
import stackpot.stackpot.badge.entity.Badge;
import stackpot.stackpot.pot.entity.mapping.PotMember;
import stackpot.stackpot.badge.entity.mapping.PotMemberBadge;
import stackpot.stackpot.badge.repository.PotMemberBadgeRepository;
import stackpot.stackpot.todo.dto.MyPotTodoResponseDTO;
import stackpot.stackpot.todo.dto.MyPotTodoUpdateRequestDTO;
import stackpot.stackpot.todo.entity.enums.TodoStatus;
import stackpot.stackpot.todo.entity.mapping.UserTodo;
import stackpot.stackpot.todo.repository.UserTodoRepository;
import stackpot.stackpot.pot.repository.PotMemberRepository;
import stackpot.stackpot.todo.dto.UserTodoTopMemberDto;
import stackpot.stackpot.user.entity.User;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserTodoServiceImpl implements UserTodoService {

    private final UserTodoRepository userTodoRepository;
    private final UserTodoConverter userTodoConverter;
    private final PotMemberRepository potMemberRepository;
    private final PotMemberBadgeRepository potMemberBadgeRepository;
    private final MyPotRepository myPotRepository;
    private final PotRepository potRepository;
    private final BadgeService badgeService;
    private final AuthService authService;
    private static final Long DEFAULT_BADGE_ID = 1L;

    @Transactional
    @Override
    public void assignBadgeToTopMembers(Long potId) {
        List<Object[]> topUsers = userTodoRepository.findTop2UsersWithMostTodos(potId);

        if (topUsers.isEmpty()) {
            return;
        }

        List<UserTodoTopMemberDto> topMemberDtos = userTodoConverter.toTopMemberDto(topUsers);

        List<PotMember> topPotMembers = topMemberDtos.stream()
                .map(user -> potMemberRepository.findByPot_PotIdAndUser_Id(potId, user.getUserId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        Badge badge = badgeService.getDefaultBadge(); // 변경: 기본 뱃지(1번 뱃지) 가져오기

        for (PotMember potMember : topPotMembers) {
            PotMemberBadge potMemberBadge = PotMemberBadge.builder()
                    .badge(badge)
                    .potMember(potMember)
                    .build();
            potMemberBadgeRepository.save(potMemberBadge);
        }

    }

    @Transactional
    @Override
    public Page<MyPotTodoResponseDTO> getTodo(Long potId, PageRequest pageRequest) {
        User user = authService.getCurrentUser();

        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        boolean isOwner = pot.getUser().equals(user);
        boolean isMember = potMemberRepository.existsByPotAndUser(pot, user);

        if (!isOwner && !isMember) {
            throw new PotHandler(ErrorStatus.POT_FORBIDDEN);
        }

        List<User> allPotMembers = potMemberRepository.findByPotId(pot.getPotId())
                .stream()
                .map(PotMember::getUser)
                .collect(Collectors.toList());

        allPotMembers.sort((u1, u2) -> u1.equals(user) ? -1 : (u2.equals(user) ? 1 : 0));

        int totalUsers = allPotMembers.size();
        int startIndex = (int) pageRequest.getOffset();
        int endIndex = Math.min(startIndex + pageRequest.getPageSize(), totalUsers);

        if (startIndex >= totalUsers) {
            return new PageImpl<>(List.of(), pageRequest, totalUsers);
        }

        List<User> pagedUsers = allPotMembers.subList(startIndex, endIndex);

        List<UserTodo> todos = myPotRepository.findByPotAndUsers(pot, pagedUsers);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayAt3AM = LocalDateTime.of(now.toLocalDate(), LocalTime.of(3, 00));
        LocalDateTime yesterdayAt3AM = todayAt3AM.minusDays(1);

        List<UserTodo> filteredTodos = todos.stream()
                .filter(todo -> todo.getCreatedAt().isAfter(yesterdayAt3AM))
                .collect(Collectors.toList());

        Map<User, List<UserTodo>> groupedByUser = filteredTodos.stream()
                .collect(Collectors.groupingBy(UserTodo::getUser));

        List<MyPotTodoResponseDTO> responseList = pagedUsers.stream()
                .map(member -> userTodoConverter.toDto(
                        member,
                        pot,
                        groupedByUser.getOrDefault(member, List.of()),
                        user))
                .collect(Collectors.toList());

        return new PageImpl<>(responseList, pageRequest, totalUsers);
    }


    @Transactional
    @Override
    public List<MyPotTodoResponseDTO> updateTodos(Long potId, List<MyPotTodoUpdateRequestDTO> reqs) {
        User user = authService.getCurrentUser();

        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        List<UserTodo> existing = myPotRepository.findByPot_PotIdAndUser(potId, user);
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
                        .user(user).pot(pot)
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

        return userTodoConverter.toListDto(pot, toSave);
    }

    @Transactional
    @Override
    public List<MyPotTodoResponseDTO> completeTodo(Long potId, Long todoId) {
        User user = authService.getCurrentUser();

        UserTodo todo = myPotRepository.findByTodoIdAndPot_PotId(todoId, potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.USER_TODO_NOT_FOUND));
        if (!todo.getUser().equals(user)) throw new PotHandler(ErrorStatus.USER_TODO_UNAUTHORIZED);

        todo.setStatus(todo.getStatus() == TodoStatus.COMPLETED
                ? TodoStatus.NOT_STARTED : TodoStatus.COMPLETED);
        myPotRepository.save(todo);

        List<UserTodo> all = myPotRepository.findByPot_PotId(potId);
        return userTodoConverter.toListDto(todo.getPot(), all);
    }

}

