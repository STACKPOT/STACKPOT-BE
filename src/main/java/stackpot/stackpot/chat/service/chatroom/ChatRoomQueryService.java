package stackpot.stackpot.chat.service.chatroom;

import stackpot.stackpot.chat.dto.ChatRoomDto;

public interface ChatRoomQueryService {

    Long selectPotIdByChatRoomId(Long chatRoomId);
    ChatRoomDto.ChatRoomNameDto selectChatRoomIdByPotId(Long potId);
}
