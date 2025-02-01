package stackpot.stackpot.converter;

import stackpot.stackpot.web.dto.UserTodoTopMemberDto;

import java.util.List;

public interface UserTodoConverter {
    List<UserTodoTopMemberDto> toTopMemberDto(List<Object[]> userTodoData);
}
