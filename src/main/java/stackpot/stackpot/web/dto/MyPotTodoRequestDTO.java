package stackpot.stackpot.web.dto;

import jakarta.persistence.*;
import lombok.*;
import stackpot.stackpot.domain.enums.TodoStatus;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPotTodoRequestDTO {
    private String content;
    private TodoStatus status;
}
