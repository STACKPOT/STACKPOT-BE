package stackpot.stackpot.task.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import stackpot.stackpot.task.entity.enums.TaskboardStatus;

import java.time.LocalDate;
import java.util.List;

public class MyPotTaskRequestDto {
    @Data
    @Getter
    @NoArgsConstructor
    public static class create{
        private String title;
        private LocalDate deadline;
        private TaskboardStatus taskboardStatus;
        private String description;
        private List<Long> participants;
    }

}
