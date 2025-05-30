package stackpot.stackpot.chat.service.chatroom;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stackpot.stackpot.chat.entity.ChatRoom;
import stackpot.stackpot.chat.repository.ChatRoomRepository;
import stackpot.stackpot.pot.entity.Pot;

@Service
@RequiredArgsConstructor
public class ChatRoomCommandService  {

    private final ChatRoomRepository chatRoomRepository;

    public void createChatRoom(String roomName, Pot pot) {
        ChatRoom chatRoom = ChatRoom.builder()
                .chatRoomName(roomName)
                .pot(pot)
                .build();

        chatRoomRepository.save(chatRoom);
    }
}
