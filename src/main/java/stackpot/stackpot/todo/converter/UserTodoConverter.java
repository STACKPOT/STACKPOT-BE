package stackpot.stackpot.todo.converter;

import stackpot.stackpot.todo.dto.UserTodoTopMemberDto;

import java.util.List;

public interface UserTodoConverter {
    List<UserTodoTopMemberDto> toTopMemberDto(List<Object[]> userTodoData);
}
