package stackpot.stackpot.todo.converter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.common.util.RoleNameMapper;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.todo.dto.MyPotTodoResponseDTO;
import stackpot.stackpot.todo.entity.enums.TodoStatus;
import stackpot.stackpot.todo.entity.mapping.UserTodo;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.entity.enums.Role;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserTodoConverter {

    // ✅ 멤버 역할(Role)을 직접 받도록 변경
    public MyPotTodoResponseDTO toDto(
            User member,
            Role memberRole,          // ← PotMember.roleName
            Pot pot,
            List<UserTodo> todos,
            User current) {

        String roleName = memberRole != null ? memberRole.name() : Role.UNKNOWN.name();
        String nicknameWithRole = member.getNickname() + " " + RoleNameMapper.mapRoleName(roleName);

        Integer notStartedCount = null;
        if (member.equals(current)) {
            notStartedCount = (int) todos.stream()
                    .filter(t -> t.getStatus() == TodoStatus.NOT_STARTED)
                    .count();
        }

        List<MyPotTodoResponseDTO.TodoDetailDTO> details = todos.stream()
                .map(t -> MyPotTodoResponseDTO.TodoDetailDTO.builder()
                        .todoId(t.getTodoId())
                        .content(t.getContent())
                        .status(t.getStatus())
                        .build())
                .collect(Collectors.toList());

        return MyPotTodoResponseDTO.builder()
                .userNickname(nicknameWithRole)
                .userRole(roleName)        // ← User.role이 아니라 PotMember.roleName 기반
                .userId(member.getId())
                .todoCount(notStartedCount)
                .todos(details.isEmpty() ? null : details)
                .build();
    }

    // 필요 시: 리스트 변환에서 roleMap을 함께 받는 버전
    public List<MyPotTodoResponseDTO> toListDtoWithRoles(
            Pot pot,
            List<UserTodo> todos,
            Map<User, Role> roleMap,
            User current) {

        Map<User, List<UserTodo>> grouped = todos.stream()
                .collect(Collectors.groupingBy(UserTodo::getUser));

        return grouped.entrySet().stream()
                .map(e -> toDto(
                        e.getKey(),
                        roleMap.getOrDefault(e.getKey(), Role.UNKNOWN),
                        pot,
                        e.getValue(),
                        current))
                .collect(Collectors.toList());
    }
}