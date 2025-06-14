package stackpot.stackpot.chat.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NewChatEvent {
    private Long chatRoomId;
}
