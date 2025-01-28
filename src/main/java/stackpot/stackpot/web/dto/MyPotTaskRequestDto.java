package stackpot.stackpot.web.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import stackpot.stackpot.domain.enums.TaskboardStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class MyPotTaskRequestDto {
    @Data
    @Getter
    @NoArgsConstructor
    public static class create{
        private String title;
        private LocalDateTime deadline;
        private TaskboardStatus taskboardStatus;
        private String description;
        private List<Long> participants;
    }

}
