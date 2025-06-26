package stackpot.stackpot.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Comparator;

public class ChatRoomResponseDto {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRoomListDto implements Comparable<ChatRoomListDto> {
        private Long chatRoomId;
        private String chatRoomName;
        private String thumbnailUrl;
        private LocalDateTime lastChatTime;
        private String lastChat;
        private int unReadMessageCount;

        @Override
        public int compareTo(ChatRoomListDto o) {
            int timeCompare = Comparator.nullsFirst(LocalDateTime::compareTo)
                    .compare(o.getLastChatTime(), this.getLastChatTime());
            if (timeCompare != 0) {
                return timeCompare;
            }
            return Integer.compare(o.getUnReadMessageCount(), this.getUnReadMessageCount());
        }
    }
}
