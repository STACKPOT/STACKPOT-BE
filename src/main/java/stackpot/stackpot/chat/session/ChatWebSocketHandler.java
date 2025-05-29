package stackpot.stackpot.chat.session;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatSessionManager chatSessionManager;

    // WebSocket 연결 시 자동 호출 (채팅방 입장)
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserId(session);
        Long chatRoomId = getChatRoomId(session);
        chatSessionManager.enterChatRoom(userId, chatRoomId, session);
    }

    // WebSocket 연결 해제 시 자동 호출 (채팅방 퇴장)
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserId(session);
        Long chatRoomId = getChatRoomId(session);
        chatSessionManager.exitChatRoom(userId, chatRoomId, session);
    }

    private Long getUserId(WebSocketSession session) {
        return (Long) session.getAttributes().get("userId");
    }

    private Long getChatRoomId(WebSocketSession session) {
        return (Long) session.getAttributes().get("chatRoomId");
    }
}
