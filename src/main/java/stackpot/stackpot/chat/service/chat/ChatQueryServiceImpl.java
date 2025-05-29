package stackpot.stackpot.chat.service.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.ChatHandler;
import stackpot.stackpot.chat.converter.ChatConverter;
import stackpot.stackpot.chat.dto.ChatDto;
import stackpot.stackpot.chat.dto.mongo.ChatId;
import stackpot.stackpot.chat.dto.response.ChatResponseDto;
import stackpot.stackpot.chat.entity.Chat;
import stackpot.stackpot.chat.repository.ChatRepository;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatQueryServiceImpl implements ChatQueryService {

    private final ChatRepository chatRepository;
    private final ChatConverter chatConverter;

    @Override
    public ChatResponseDto.AllChatDto selectAllChatsInChatRoom(Long chatRoomId, String cursor, int size, String direction) {
        /*
            1. JWT -> userId
            2. chatRoomId -> potId
            3. userId + potId -> potMemberId
            4. potMemberId + chatRoomId -> chatRoomInfo의 lastReadChatId

            - 첫 요청은 lastReadChatId를 기준으로 페이지네이션
            - direction이 prev면 위로 DESC 페이지네이션, next면 아래로 ASC 페이지네이션
         */
        PageRequest pageRequest = PageRequest.of(0, size);
        List<Chat> chats;

        if (cursor == null || cursor.isEmpty()) {
            String lastReadChatId = "lastReadChatId";
            chats = chatRepository.findByChatRoomIdAndIdGreaterThanOrderByIdAsc(chatRoomId, lastReadChatId, pageRequest);
        } else if (direction.equals("prev")) {
            chats = chatRepository.findByChatRoomIdAndIdLessThanOrderByIdDesc(chatRoomId, cursor, pageRequest);
            Collections.reverse(chats);
        } else if (direction.equals("next")) {
            chats = chatRepository.findByChatRoomIdAndIdGreaterThanOrderByIdAsc(chatRoomId, cursor, pageRequest);
        } else {
            throw new ChatHandler(ErrorStatus.CHAT_BAD_REQUEST);
        }
        String prevCursor = !chats.isEmpty() ? chats.get(0).getId() : null;
        String nextCursor = !chats.isEmpty() ? chats.get(chats.size() - 1).getId() : null;

        return chatConverter.toAllChatDto(prevCursor, nextCursor, chats);
    }

    @Override
    public String selectLatestChatId(Long chatRoomId) {
        ChatId chatId = chatRepository.findChatIdFirstByChatRoomIdOrderByIdDesc(chatRoomId);
        if (chatId == null)
            throw new ChatHandler(ErrorStatus.CHAT_NOT_FOUND);
        return chatId.getId();
    }

    @Override
    public ChatDto.LastChatDto selectLastChatInChatRoom(Long chatRoomId) {
        Chat chat = chatRepository.findChatFirstByChatRoomIdOrderByIdDesc(chatRoomId);
        if (chat == null)
            throw new ChatHandler(ErrorStatus.CHAT_NOT_FOUND);
        return ChatDto.LastChatDto.builder()
                .lastChat(chat.getMessage())
                .lastChatTime(chat.getCreatedAt())
                .build();
    }

    @Override
    public int getUnReadMessageCount(Long chatRoomId, String lastReadChatId) {
        return chatRepository.countByChatRoomIdAndIdGreaterThan(chatRoomId, lastReadChatId);
    }
}
