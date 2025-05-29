package stackpot.stackpot.chat.session;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class StompHandler implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // TODO JWT 검증

        // js에서 {token: ~~ , chatRoomId : ~ } 이렇게 헤더 만들어서 보내면 됨
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String chatRoomId = accessor.getFirstNativeHeader("chatRoomId");
            String accessToken = accessor.getFirstNativeHeader("token");

            // TODO JWT 파싱 -> userId 저장
            Long userId = 1L;
            accessor.getSessionAttributes().put("chatRoomId", chatRoomId);
            accessor.getSessionAttributes().put("userId", userId);
        }
        return message;
    }
}
