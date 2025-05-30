package stackpot.stackpot.chat.session;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import stackpot.stackpot.config.security.JwtTokenProvider;

@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // js에서 {token: ~~ , chatRoomId : ~ } 이렇게 헤더 만들어서 보내면 됨
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String chatRoomId = accessor.getFirstNativeHeader("chatRoomId");
            String accessToken = accessor.getFirstNativeHeader("token");
            Long userId = jwtTokenProvider.extractUserIdFromJwt(accessToken);

            accessor.getSessionAttributes().put("chatRoomId", chatRoomId);
            accessor.getSessionAttributes().put("userId", userId);
        }
        return message;
    }
}
