package stackpot.stackpot.chat.converter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.chat.dto.response.ChatResponseDto;
import stackpot.stackpot.chat.entity.Chat;

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
                .message(chat.getMessage())
                .fileUrl(chat.getFileUrl())
                .createdAt(chat.getCreatedAt())
                .build();
    }

    public ChatResponseDto.AllChatDto toAllChatDto(String prevCursor, String nextCursor, List<Chat> chats) {
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
