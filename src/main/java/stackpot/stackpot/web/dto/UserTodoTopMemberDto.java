package stackpot.stackpot.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserTodoTopMemberDto {
    private Long userId;
    private Long todoCount;
}
