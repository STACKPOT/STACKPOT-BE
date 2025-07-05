package stackpot.stackpot.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import stackpot.stackpot.user.entity.enums.Role;

import java.time.LocalDateTime;
import java.util.List;

public class ChatResponseDto {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatFileDto {
        private String fileUrl;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatDto {
        private Long userId;
        private Long chatId;
        private String userName;
        private Role role;
        private String message;
        private String fileUrl;
        private String createdAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllChatDto {
        private Long prevCursor;
        private Long nextCursor;
        List<ChatDto> chats;
    }
}
