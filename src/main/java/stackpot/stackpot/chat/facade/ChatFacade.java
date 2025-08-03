package stackpot.stackpot.chat.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import stackpot.mongo.Chat;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.ChatHandler;
import stackpot.stackpot.chat.converter.ChatConverter;
import stackpot.stackpot.chat.dto.request.ChatRequestDto;
import stackpot.stackpot.chat.dto.response.ChatResponseDto;
import stackpot.stackpot.chat.service.chat.ChatCommandService;
import stackpot.stackpot.chat.service.chat.ChatFileService;
import stackpot.stackpot.chat.service.chat.ChatQueryService;
import stackpot.stackpot.chat.service.chat.ChatSendService;
import stackpot.stackpot.chat.service.chatroom.ChatRoomQueryService;
import stackpot.stackpot.chat.service.chatroominfo.ChatRoomInfoCommandService;
import stackpot.stackpot.chat.service.chatroominfo.ChatRoomInfoQueryService;
import stackpot.stackpot.event.SseService;
import stackpot.stackpot.chat.session.ChatSessionManager;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.pot.service.potMember.PotMemberQueryService;
import stackpot.stackpot.user.entity.enums.Role;
import stackpot.stackpot.user.service.UserQueryService;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatFacade {

    private final ChatQueryService chatQueryService;
    private final ChatCommandService chatCommandService;
    private final ChatRoomQueryService chatRoomQueryService;
    private final ChatRoomInfoCommandService chatRoomInfoCommandService;
    private final ChatRoomInfoQueryService chatRoomInfoQueryService;
    private final PotMemberQueryService potMemberQueryService;
    private final UserQueryService userQueryService;
    private final AuthService authService;
    private final SseService sseService;
    private final ChatSendService chatSendService;
    private final ChatFileService chatFileService;
    private final ChatSessionManager chatSessionManager;
    private final ChatConverter chatConverter;

    public void chat(ChatRequestDto.ChatMessageDto chatMessageDto, Long userId, Long chatRoomId) {
        Long potId = chatRoomQueryService.selectPotIdByChatRoomId(chatRoomId);
        Role role = potMemberQueryService.selectRoleByUserIdAndPotId(userId, potId);
        String userName = userQueryService.selectNameByUserId(userId);

        Chat chat = chatCommandService.saveChatMessage(chatMessageDto, userId, userName, chatRoomId, role); // 1. 채팅 DB에 저장
        chatSendService.sendMessage(chat, chatRoomId); // 2. 채팅 실시간 전송

        List<Long> userIds = chatSessionManager.getOnlineUserIds(chatRoomId);
        List<Long> potMemberIds = potMemberQueryService.selectPotMembersIdsByUserIdsAndPotId(userIds, potId);

        chatRoomInfoCommandService.updateLastReadChatId(potMemberIds, chatRoomId, chat.getId()); // 3. 채팅방에 접속한 사용자들의 마지막으로 읽은 채팅 메시지 Id 업데이트

        sseService.sendChatRoomList(chatRoomId); // 4. 채팅방 리스트 SSE 전송
    }

    public ChatResponseDto.AllChatDto selectAllChatsInChatRoom(Long chatRoomId, Long cursor, int size, String direction) {
         /*
            - 첫 요청은 lastReadChatId를 기준으로 페이지네이션
            - direction이 prev면 위로 DESC 페이지네이션, next면 아래로 ASC 페이지네이션
         */
        List<Chat> chats;
        if (cursor == null) {
            Long userId = authService.getCurrentUserId();
            Long potId = chatRoomQueryService.selectPotIdByChatRoomId(chatRoomId);
            Long potMemberId = potMemberQueryService.selectPotMemberIdByUserIdAndPotId(userId, potId);
            Long lastReadChatId = chatRoomInfoQueryService.selectLastReadChatIdByPotMemberIdAndChatRoomId(potMemberId, chatRoomId);
            chats = chatQueryService.selectAllChatsInChatRoom(chatRoomId, size, lastReadChatId);
        } else if (direction.equals("prev") || direction.equals("next")) {
            chats = chatQueryService.selectAllChatsInChatRoom(chatRoomId, cursor, size, direction);
        } else {
            throw new ChatHandler(ErrorStatus.CHAT_BAD_REQUEST);
        }

        Long prevCursor = !chats.isEmpty() ? chats.get(0).getId() : null;
        Long nextCursor = !chats.isEmpty() ? chats.get(chats.size() - 1).getId() : null;
        return chatConverter.toAllChatDto(prevCursor, nextCursor, chats);
    }

    public ChatResponseDto.ChatFileDto saveFileInS3(MultipartFile file) {
        return chatFileService.saveFileInS3(file);
    }
}
