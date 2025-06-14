package stackpot.stackpot.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ChatDto {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LastChatDto {
        private String lastChat;
        private LocalDateTime lastChatTime;
    }
}
