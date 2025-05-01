package stackpot.stackpot.todo.converter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.todo.dto.UserTodoTopMemberDto;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserTodoConverterImpl implements UserTodoConverter {

    @Override
    public List<UserTodoTopMemberDto> toTopMemberDto(List<Object[]> userTodoData) {
        return userTodoData.stream()
                .map(result -> new UserTodoTopMemberDto(
                        (Long) result[0], // userId
                        (Long) result[1]  // todoCount
                ))
                .collect(Collectors.toList());
    }
}
