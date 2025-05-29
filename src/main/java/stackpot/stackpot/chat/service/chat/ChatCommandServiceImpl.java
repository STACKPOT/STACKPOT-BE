package stackpot.stackpot.chat.service.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stackpot.stackpot.chat.dto.request.ChatRequestDto;
import stackpot.stackpot.chat.entity.Chat;
import stackpot.stackpot.chat.repository.ChatRepository;

@Service
@RequiredArgsConstructor
public class ChatCommandServiceImpl implements ChatCommandService {

    private final ChatRepository chatRepository;

    @Override
    public Chat saveChatMessage(final ChatRequestDto.ChatMessageDto chatMessageDto) {
        Chat chat = Chat.builder()
                .userId(1L)
                .userName("test")
                .chatRoomId(chatMessageDto.getRoomId())
                .message(chatMessageDto.getMessage())
                .fileUrl(chatMessageDto.getFileUrl())
                .build();

        return chatRepository.save(chat);
    }
}
