package stackpot.stackpot.todo.converter;

import stackpot.stackpot.common.util.RoleNameMapper;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.todo.dto.MyPotTodoResponseDTO;

import java.util.List;

import org.springframework.stereotype.Component;
import stackpot.stackpot.todo.entity.mapping.UserTodo;
import stackpot.stackpot.user.entity.User;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserTodoConverter{

    public MyPotTodoResponseDTO toDto(
            User member,
            Pot pot,
            List<UserTodo> todos,
            User current) {

        String role = String.valueOf(member.getRole());
        String nicknameWithRole = member.getNickname() + " " + RoleNameMapper.mapRoleName(role);

        Integer notStartedCount = null;
        if (member.equals(current)) {
            notStartedCount = (int) todos.stream()
                    .filter(t -> t.getStatus()==stackpot.stackpot.todo.entity.enums.TodoStatus.NOT_STARTED)
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
                .userRole(member.getRole().name())
                .userId(member.getId())
                .todoCount(notStartedCount)
                .todos(details.isEmpty()? null : details)
                .build();
    }


    public List<MyPotTodoResponseDTO> toListDto(Pot pot, List<UserTodo> todos) {
        Map<User, List<UserTodo>> grouped = todos.stream()
                .collect(Collectors.groupingBy(UserTodo::getUser));

        return grouped.entrySet().stream()
                .map(e -> toDto(e.getKey(), pot, e.getValue(), e.getKey()))
                .collect(Collectors.toList());
    }

}
