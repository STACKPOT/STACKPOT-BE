package stackpot.stackpot.chat.service.chatroom;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.ChatHandler;
import stackpot.stackpot.chat.dto.ChatRoomDto;
import stackpot.stackpot.chat.entity.ChatRoom;
import stackpot.stackpot.chat.repository.ChatRoomRepository;

@Service
@RequiredArgsConstructor
public class ChatRoomQueryService {

    private final ChatRoomRepository chatRoomRepository;

    public Long selectPotIdByChatRoomId(Long chatRoomId) {
        return chatRoomRepository.findPotIdByChatRoomId(chatRoomId).orElseThrow(() -> new ChatHandler(ErrorStatus.CHATROOM_NOT_FOUND));
    }

    public Long selectChatRoomIdByPotId(Long potId){
        return chatRoomRepository.selectChatRoomIdByPotId(potId).orElseThrow(() -> new ChatHandler(ErrorStatus.CHATROOM_NOT_FOUND));
    }

    public ChatRoomDto.ChatRoomNameDto selectChatRoomNameDtoIdByPotId(Long potId) {
        return chatRoomRepository.findChatRoomNameDtoIdByPotId(potId).orElse(null);
    }
}
