package stackpot.stackpot.chat.service.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import stackpot.mongo.Chat;
import stackpot.mongo.ChatId;
import stackpot.mongo.ChatRepository;
import stackpot.stackpot.chat.dto.ChatDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatQueryService {

    private final ChatRepository chatRepository;

    public List<Chat> selectAllChatsInChatRoom(Long chatRoomId, int size, Long lastReadChatId) {
        PageRequest pageRequest = PageRequest.of(0, size);
        List<Chat> chats;
        if (lastReadChatId == null) {
            chats = chatRepository.findByChatRoomIdOrderByIdAsc(chatRoomId, pageRequest);
        } else {
            chats = chatRepository.findByChatRoomIdAndIdLessThanEqualOrderByIdDesc(chatRoomId, lastReadChatId, pageRequest);
            Collections.reverse(chats);
        }
        return chats;
    }

    public List<Chat> selectAllChatsInChatRoom(Long chatRoomId, Long cursor, int size, String direction) {
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

    public Long selectLatestChatId(Long chatRoomId) {
        ChatId chatId = chatRepository.findFirstChatIdByChatRoomIdOrderByIdDesc(chatRoomId).orElse(null);
        if (chatId == null)
            return null;
        return chatId.getId();
    }

    public ChatDto.LastChatDto selectLastChatInChatRoom(Long chatRoomId) {
        Chat chat = chatRepository.findFirstByChatRoomIdOrderByIdDesc(chatRoomId).orElse(null);
        if (chat == null) {
            return ChatDto.LastChatDto.builder()
                    .lastChat("")
                    .lastChatTime(null)
                    .build();
        }
        return ChatDto.LastChatDto.builder()
                .lastChat(chat.getMessage())
                .lastChatTime(chat.getUpdatedAt())
                .build();
    }

    public int getUnReadMessageCount(Long chatRoomId, Long lastReadChatId) {
        if (lastReadChatId == null) {
            return chatRepository.countByChatRoomId(chatRoomId);
        }
        return chatRepository.countByChatRoomIdAndIdGreaterThan(chatRoomId, lastReadChatId);
    }
}
