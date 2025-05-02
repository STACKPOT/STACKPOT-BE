package stackpot.stackpot.todo.converter;

import stackpot.stackpot.todo.dto.UserTodoTopMemberDto;

import java.util.List;

import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class UserTodoConverter{

    public List<UserTodoTopMemberDto> toTopMemberDto(List<Object[]> userTodoData) {
        return userTodoData.stream()
                .map(result -> new UserTodoTopMemberDto(
                        (Long) result[0], // userId
                        (Long) result[1]  // todoCount
                ))
                .collect(Collectors.toList());
    }
}
