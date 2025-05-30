package stackpot.stackpot.chat.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import stackpot.stackpot.chat.dto.request.ChatRequestDto;
import stackpot.stackpot.chat.dto.response.ChatResponseDto;
import stackpot.stackpot.chat.entity.Chat;
import stackpot.stackpot.chat.service.chat.ChatCommandService;
import stackpot.stackpot.chat.service.chat.ChatFileService;
import stackpot.stackpot.chat.service.chat.ChatQueryService;
import stackpot.stackpot.chat.service.chat.ChatSendService;
import stackpot.stackpot.chat.service.chatroom.ChatRoomQueryService;
import stackpot.stackpot.chat.service.chatroominfo.ChatRoomInfoCommandService;
import stackpot.stackpot.chat.session.ChatSessionManager;
import stackpot.stackpot.pot.service.PotMemberQueryService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatFacade {

    private final ChatQueryService chatQueryService;
    private final ChatCommandService chatCommandService;
    private final ChatRoomQueryService chatRoomQueryService;
    private final ChatRoomInfoCommandService chatRoomInfoCommandService;
    private final PotMemberQueryService potMemberQueryService;
    private final ChatSendService chatSendService;
    private final ChatFileService chatFileService;
    private final ChatSessionManager chatSessionManager;

    public void chat(ChatRequestDto.ChatMessageDto chatMessageDto, Long userId, Long chatRoomId) {
        Chat chat = chatCommandService.saveChatMessage(chatMessageDto, userId);
        chatSendService.sendMessage(chat, chatRoomId);
        Long potId = chatRoomQueryService.selectPotIdByChatRoomId(chatRoomId);
        List<Long> userIds = chatSessionManager.getOnlineUserIds(chatRoomId);
        List<Long> potMemberIds = potMemberQueryService.selectPotMembersIdsByUserIdsAndPotId(userIds, potId);

        chatRoomInfoCommandService.updateLastReadChatId(potMemberIds, chatRoomId, chat.getId());
    }

    public ChatResponseDto.AllChatDto selectAllChatsInChatRoom(Long chatRoomId, String cursor, int size, String direction) {
        return chatQueryService.selectAllChatsInChatRoom(chatRoomId, cursor, size, direction);
    }

    public ChatResponseDto.ChatFileDto saveFileInS3(MultipartFile file) {
        return chatFileService.saveFileInS3(file);
    }
}
