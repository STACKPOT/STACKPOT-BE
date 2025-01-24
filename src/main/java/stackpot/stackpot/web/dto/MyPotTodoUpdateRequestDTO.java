package stackpot.stackpot.web.dto;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPotTodoUpdateRequestDTO {
    private Long todoId;
    private String content;
}
