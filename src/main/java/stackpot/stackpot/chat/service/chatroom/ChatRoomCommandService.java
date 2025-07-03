package stackpot.stackpot.chat.service.chatroom;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.PotHandler;
import stackpot.stackpot.chat.entity.ChatRoom;
import stackpot.stackpot.chat.repository.ChatRoomRepository;
import stackpot.stackpot.pot.entity.Pot;

@Service
@RequiredArgsConstructor
public class ChatRoomCommandService  {

    private final ChatRoomRepository chatRoomRepository;

    public Long createChatRoom(String roomName, Pot pot) {
        ChatRoom chatRoom = ChatRoom.builder()
                .chatRoomName(roomName)
                .pot(pot)
                .build();

        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        return savedChatRoom.getId();
    }

    @Transactional
    public void deleteChatRoomByPotId(Long potId){
        chatRoomRepository.deleteByPotId(potId);
    }
}
