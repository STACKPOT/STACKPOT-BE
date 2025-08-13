package stackpot.stackpot.chat.converter;

import org.springframework.stereotype.Component;
import stackpot.mongo.Chat;
import stackpot.stackpot.chat.dto.response.ChatResponseDto;
import stackpot.stackpot.chat.dto.response.ChatRoomResponseDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static stackpot.stackpot.user.entity.enums.Role.UNKNOWN;

@Component
public class ChatConverter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 M월 d일 H:mm");

    public ChatResponseDto.ChatFileDto toChatFileDto(String fileUrl) {
        return ChatResponseDto.ChatFileDto.builder()
                .fileUrl(fileUrl)
                .build();
    }

    public ChatResponseDto.ChatDto toChatDto(Chat chat) {

        String userNameWithRole;

        if (chat.getUserName() == null || chat.getUserName().equals("(알 수 없음)") || chat.getRole() == null) {
            userNameWithRole = chat.getUserName();
        } else {
            userNameWithRole = chat.getUserName() + " " + chat.getRole().getVegetable();
        }
        return ChatResponseDto.ChatDto.builder()
                .userId(chat.getUserId())
                .chatId(chat.getId())
                .userName(userNameWithRole)
                .role(chat.getRole())
                .message(chat.getMessage())
                .fileUrl(chat.getFileUrl())
                .createdAt(chat.getUpdatedAt().format(DATE_FORMATTER))
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

    public ChatRoomResponseDto.ChatRoomListDto toChatRoomListDto(Long chatRoomId, String chatRoomName, String thumbnailUrl,
                                                                 LocalDateTime lastChatTime, String lastChat, int unReadMessageCount) {
        return ChatRoomResponseDto.ChatRoomListDto.builder()
                .chatRoomId(chatRoomId)
                .chatRoomName(chatRoomName)
                .thumbnailUrl(thumbnailUrl)
                .lastChatTime(lastChatTime)
                .lastChat(lastChat)
                .unReadMessageCount(unReadMessageCount)
                .build();
    }
}
