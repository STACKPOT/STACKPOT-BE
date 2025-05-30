package stackpot.stackpot.chat.service.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stackpot.mongo.ChatRepository;
import stackpot.stackpot.chat.dto.request.ChatRequestDto;
import stackpot.stackpot.chat.entity.Chat;
import stackpot.stackpot.user.service.UserQueryService;

@Service
@RequiredArgsConstructor
public class ChatCommandService {

    private final UserQueryService userQueryService;
    private final ChatRepository chatRepository;

    public Chat saveChatMessage(final ChatRequestDto.ChatMessageDto chatMessageDto, Long userId) {
        String userName = userQueryService.selectNameByUserId(userId);
        Chat chat = Chat.builder()
                .userId(userId)
                .userName(userName)
                .chatRoomId(chatMessageDto.getRoomId())
                .message(chatMessageDto.getMessage())
                .fileUrl(chatMessageDto.getFileUrl())
                .build();

        return chatRepository.save(chat);
    }
}
