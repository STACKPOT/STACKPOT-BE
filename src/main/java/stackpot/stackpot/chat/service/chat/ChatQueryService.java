package stackpot.stackpot.chat.service.chat;

import stackpot.stackpot.chat.dto.ChatDto;
import stackpot.stackpot.chat.dto.response.ChatResponseDto;

public interface ChatQueryService {

    ChatResponseDto.AllChatDto selectAllChatsInChatRoom(Long chatRoomId, String cursor, int size, String direction);

    String selectLatestChatId(Long chatRoomId);

    ChatDto.LastChatDto selectLastChatInChatRoom(Long chatRoomId);

    int getUnReadMessageCount(Long chatRoomId, String lastReadChatId);
}
