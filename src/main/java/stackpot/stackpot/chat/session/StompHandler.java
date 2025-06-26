package stackpot.stackpot.chat.session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import stackpot.stackpot.config.security.JwtTokenProvider;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String chatRoomId = accessor.getFirstNativeHeader("ChatRoomId");
            String accessToken = accessor.getFirstNativeHeader("Authorization");
            Long userId = jwtTokenProvider.extractUserIdFromJwt(accessToken);

            accessor.getSessionAttributes().put("chatRoomId", chatRoomId);
            accessor.getSessionAttributes().put("userId", userId);
        }
        return message;
    }
}
