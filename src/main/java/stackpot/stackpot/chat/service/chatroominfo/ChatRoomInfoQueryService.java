package stackpot.stackpot.chat.service.chatroominfo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.chat.dto.response.ChatRoomResponseDto;

import java.util.List;

public interface ChatRoomInfoQueryService {

    void registerPolling(DeferredResult<ResponseEntity<ApiResponse<List<ChatRoomResponseDto.ChatRoomListDto>>>> deferredResult);

    String selectLastReadChatIdByPotMemberIdAndChatRoomId(Long potMemberId, Long chatRoomId);
}
