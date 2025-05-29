package stackpot.stackpot.chat.service.chatroom;

import stackpot.stackpot.pot.entity.Pot;

public interface ChatRoomCommandService {

    void createChatRoom(String roomName, Pot pot);
}
