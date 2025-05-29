package stackpot.stackpot.chat.service.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import stackpot.stackpot.chat.converter.ChatConverter;
import stackpot.stackpot.chat.entity.Chat;
import stackpot.stackpot.chat.event.NewChatEvent;

@RequiredArgsConstructor
@Service
public class ChatSendService {

    private static final String CHAT_SUB_URL = "/sub/chat";

    private final ApplicationEventPublisher eventPublisher;

    private final ChatConverter chatConverter;
    private final SimpMessagingTemplate messagingTemplate;

    public void sendMessage(final Chat chat, final Long chatRoomId) {
        messagingTemplate.convertAndSend(CHAT_SUB_URL + chatRoomId, chatConverter.toChatDto(chat));
        eventPublisher.publishEvent(new NewChatEvent(chatRoomId));
    }
}
