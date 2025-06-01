package stackpot.stackpot.chat.session;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatSessionManager {

    // chatRoomId : (userId : SessionId)
    // SessionId가 필요한 이유는 사용자가 모바일/pc/다른 브라우저 등으로 동시 접속할 수도 있어서 어디서 접속 했는 지 파악을 위함
    private final Map<Long, Map<Long, Set<String>>> sessionMap = new ConcurrentHashMap<>();

    // disconnect 할 때는 nativeHeader에서 꺼낼 수가 없기 때문에 session에 대한 userId, chatroomId 정보 저장
    private final Map<String, SessionInfo> sessionInfoMap = new ConcurrentHashMap<>();

    public void enterChatRoom(Long userId, Long chatRoomId, String sessionId) {
        sessionMap
                .computeIfAbsent(chatRoomId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                .add(sessionId);
        sessionInfoMap.put(sessionId, new SessionInfo(userId, chatRoomId));
    }

    public void exitChatRoom(String sessionId) {
        SessionInfo sessionInfo = sessionInfoMap.get(sessionId);
        if (sessionInfo != null) {
            Long userId = sessionInfo.getUserId();
            Long chatRoomId = sessionInfo.getChatRoomId();

            Map<Long, Set<String>> userSessions = sessionMap.get(chatRoomId);
            if (userSessions == null) return;

            Set<String> sessions = userSessions.get(userId);
            if (sessions == null) return;

            sessions.remove(sessionId);

            if (sessions.isEmpty()) {
                userSessions.remove(userId);
                if (userSessions.isEmpty()) {
                    sessionMap.remove(chatRoomId);
                }
            }
        }


    }

    public List<Long> getOnlineUserIds(Long chatRoomId) {
        Map<Long, Set<String>> userSessions = sessionMap.get(chatRoomId);
        return userSessions != null ? new ArrayList<>(userSessions.keySet()) : List.of();
    }


    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SessionInfo {
        private Long userId;
        private Long chatRoomId;
    }
}
