package stackpot.stackpot.chat.session;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatSessionManager {

    // chatRoomId : (userId : Session)
    private final Map<Long, Map<Long, WebSocketSession>> sessionMap = new ConcurrentHashMap<>();

    public void enterChatRoom(Long userId, Long chatRoomId, WebSocketSession session) {
        sessionMap
                .computeIfAbsent(chatRoomId, k -> new ConcurrentHashMap<>())
                .put(userId, session);
    }

    public void exitChatRoom(Long userId, Long chatRoomId, WebSocketSession session) {
        Map<Long, WebSocketSession> sessions = sessionMap.get(chatRoomId);
        if (sessions != null) {
            sessions.remove(userId);
            if (sessions.isEmpty()) {
                sessionMap.remove(chatRoomId);
            }
        }
    }

    public int getSessionCountInChatRoom(Long chatRoomId) {
        Map<Long, WebSocketSession> sessions = sessionMap.get(chatRoomId);
        return sessions != null ? sessions.size() : 0;
    }

    public List<Long> getOnlineUserIds(Long chatRoomId) {
        Map<Long, WebSocketSession> sessions = sessionMap.get(chatRoomId);
        return sessions.keySet().stream().toList();
    }
}
