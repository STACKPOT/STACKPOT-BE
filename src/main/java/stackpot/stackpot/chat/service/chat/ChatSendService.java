package stackpot.stackpot.chat.service.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import stackpot.mongo.Chat;
import stackpot.stackpot.chat.converter.ChatConverter;
import stackpot.stackpot.chat.event.NewChatEvent;

@RequiredArgsConstructor
@Service
public class ChatSendService {

    private static final String CHAT_SUB_URL = "/sub/chat/";

    private final ChatConverter chatConverter;
    private final SimpMessagingTemplate messagingTemplate;

    public void sendMessage(Chat chat, Long chatRoomId) {
        messagingTemplate.convertAndSend(CHAT_SUB_URL + chatRoomId, chatConverter.toChatDto(chat));
    }
}
