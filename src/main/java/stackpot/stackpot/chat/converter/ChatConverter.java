package stackpot.stackpot.chat.converter;

import org.springframework.stereotype.Component;
import stackpot.mongo.Chat;
import stackpot.stackpot.chat.dto.response.ChatResponseDto;
import stackpot.stackpot.user.entity.enums.Role;

import java.util.List;

@Component
public class ChatConverter {

    public ChatResponseDto.ChatFileDto toChatFileDto(String fileUrl) {
        return ChatResponseDto.ChatFileDto.builder()
                .fileUrl(fileUrl)
                .build();
    }

    public ChatResponseDto.ChatDto toChatDto(Chat chat) {
        return ChatResponseDto.ChatDto.builder()
                .chatId(chat.getId())
                .userName(chat.getUserName())
                .role(chat.getRole())
                .message(chat.getMessage())
                .fileUrl(chat.getFileUrl())
                .createdAt(chat.getUpdatedAt())
                .build();
    }

    public ChatResponseDto.AllChatDto toAllChatDto(Long prevCursor, Long nextCursor, List<Chat> chats) {
        List<ChatResponseDto.ChatDto> dtos = chats.stream()
                .map(this::toChatDto)
                .toList();

        return ChatResponseDto.AllChatDto.builder()
                .nextCursor(nextCursor)
                .prevCursor(prevCursor)
                .chats(dtos)
                .build();
    }
}
