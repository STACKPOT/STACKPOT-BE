package stackpot.stackpot.chat.session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import stackpot.stackpot.config.security.JwtTokenProvider;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final ChatSessionManager chatSessionManager;
    private final JwtTokenProvider jwtTokenProvider;

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Long chatRoomId = Long.parseLong(Objects.requireNonNull(accessor.getFirstNativeHeader("ChatRoomId")));
        String accessToken = accessor.getFirstNativeHeader("Authorization");
        Long userId = jwtTokenProvider.extractUserIdFromJwt(accessToken);

        log.info("세션 연결 : {}, {}, {}", userId, chatRoomId, sessionId);
        chatSessionManager.enterChatRoom(userId, chatRoomId, sessionId);
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        chatSessionManager.exitChatRoom(sessionId);
        log.info("세션 해제 : {}", sessionId);
    }
}
