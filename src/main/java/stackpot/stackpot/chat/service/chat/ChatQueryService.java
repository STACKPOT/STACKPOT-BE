package stackpot.stackpot.chat.service.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import stackpot.mongo.ChatId;
import stackpot.mongo.ChatRepository;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.ChatHandler;
import stackpot.stackpot.chat.dto.ChatDto;
import stackpot.mongo.Chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatQueryService {

    private final ChatRepository chatRepository;

    public List<Chat> selectAllChatsInChatRoom(Long chatRoomId, String cursor, int size, String direction, String lastReadChatId) {
        PageRequest pageRequest = PageRequest.of(0, size);
        return chatRepository.findByChatRoomIdAndIdGreaterThanOrderByIdAsc(chatRoomId, lastReadChatId, pageRequest);
    }

    public List<Chat> selectAllChatsInChatRoom(Long chatRoomId, String cursor, int size, String direction) {
        PageRequest pageRequest = PageRequest.of(0, size);
        List<Chat> chats = new ArrayList<>();
        if (direction.equals("prev")) {
            chats = chatRepository.findByChatRoomIdAndIdLessThanOrderByIdDesc(chatRoomId, cursor, pageRequest);
            Collections.reverse(chats);
        } else if (direction.equals("next")) {
            chats = chatRepository.findByChatRoomIdAndIdGreaterThanOrderByIdAsc(chatRoomId, cursor, pageRequest);
        }
        return chats;
    }


    public String selectLatestChatId(Long chatRoomId) {
        ChatId chatId = chatRepository.findChatIdFirstByChatRoomIdOrderByIdDesc(chatRoomId);
        if (chatId == null)
            throw new ChatHandler(ErrorStatus.CHAT_NOT_FOUND);
        return chatId.getId();
    }

    public ChatDto.LastChatDto selectLastChatInChatRoom(Long chatRoomId) {
        Chat chat = chatRepository.findChatFirstByChatRoomIdOrderByIdDesc(chatRoomId);
        if (chat == null)
            throw new ChatHandler(ErrorStatus.CHAT_NOT_FOUND);
        return ChatDto.LastChatDto.builder()
                .lastChat(chat.getMessage())
                .lastChatTime(chat.getCreatedAt())
                .build();
    }

    public int getUnReadMessageCount(Long chatRoomId, String lastReadChatId) {
        return chatRepository.countByChatRoomIdAndIdGreaterThan(chatRoomId, lastReadChatId);
    }
}
