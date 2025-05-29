package stackpot.stackpot.chat.service.chatroom;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.ChatHandler;
import stackpot.stackpot.chat.dto.ChatRoomDto;
import stackpot.stackpot.chat.repository.ChatRoomRepository;

@Service
@RequiredArgsConstructor
public class ChatRoomQueryServiceImpl implements ChatRoomQueryService {

    private final ChatRoomRepository chatRoomRepository;

    @Override
    public Long selectPotIdByChatRoomId(Long chatRoomId) {
        return chatRoomRepository.findPotIdByChatRoomId(chatRoomId).orElseThrow(() -> new ChatHandler(ErrorStatus.CHATROOM_NOT_FOUND));
    }

    @Override
    public ChatRoomDto.ChatRoomNameDto selectChatRoomIdByPotId(Long potId) {
        return chatRoomRepository.findChatRoomIdByPotId(potId).orElseThrow(() -> new ChatHandler(ErrorStatus.CHATROOM_NOT_FOUND));
    }
}
