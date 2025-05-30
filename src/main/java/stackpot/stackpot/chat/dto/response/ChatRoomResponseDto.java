package stackpot.stackpot.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ChatRoomResponseDto {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRoomListDto {
        private String chatRoomName;
        private String thumbnailUrl;
        private LocalDateTime lastChatTime;
        private String lastChat;
        private int unReadMessageCount;
    }
}
