package stackpot.stackpot.chat.service.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stackpot.mongo.Chat;
import stackpot.mongo.ChatRepository;
import stackpot.stackpot.chat.util.SequenceGenerator;
import stackpot.stackpot.chat.dto.request.ChatRequestDto;
import stackpot.stackpot.user.entity.enums.Role;

@Service
@RequiredArgsConstructor
public class ChatCommandService {

    private final ChatRepository chatRepository;
    private final SequenceGenerator sequenceGenerator;

    public Chat saveChatMessage(ChatRequestDto.ChatMessageDto chatMessageDto, Long userId, String userName, Long chatRoomId, Role role) {
        Chat chat = Chat.builder()
                .id(sequenceGenerator.generateSequence("chat_sequence"))
                .userId(userId)
                .userName(userName)
                .role(role)
                .chatRoomId(chatRoomId)
                .message(chatMessageDto.getMessage())
                .fileUrl(chatMessageDto.getFileUrl())
                .build();

        return chatRepository.save(chat);
    }

    public void deleteChatMessage(Long userId, Long chatRoomId) {
        chatRepository.deleteByUserIdAndChatRoomId(userId, chatRoomId);
    }
}
