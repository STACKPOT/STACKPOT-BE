package stackpot.stackpot.chat.service.chatroominfo;

import stackpot.stackpot.chat.dto.request.ChatRoomRequestDto;
import stackpot.stackpot.chat.dto.response.ChatRoomResponseDto;

import java.util.List;

public interface ChatRoomInfoCommandService {

    void updateLastReadChatId(Long userId, Long chatRoomId, String chatId);

    void joinChatRoom(ChatRoomRequestDto.ChatRoomJoinDto chatRoomJoinDto);

    void updateThumbnail(ChatRoomRequestDto.ChatRoomThumbNailDto chatRoomThumbNailDto);
}
