package stackpot.stackpot.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.chat.dto.request.ChatRoomRequestDto;
import stackpot.stackpot.chat.dto.response.ChatRoomResponseDto;
import stackpot.stackpot.chat.service.chatroominfo.ChatRoomInfoCommandService;
import stackpot.stackpot.chat.service.chatroominfo.ChatRoomInfoQueryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Chat Management", description = "채팅방 API")
@RequestMapping("/chat-rooms")
public class ChatRoomController {

    private final ChatRoomInfoCommandService chatRoomInfoCommandService;
    private final ChatRoomInfoQueryService chatRoomInfoQueryService;

    @Operation(summary = "채팅방 리스트 가져오기",
            description = "사용자가 속한 모든 채팅방 목록을 가져오는 API 입니다.")
    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<ChatRoomResponseDto.ChatRoomListDto>>> getChatRooms() {
        List<ChatRoomResponseDto.ChatRoomListDto> dtos = chatRoomInfoQueryService.selectChatRoomList();
        return ResponseEntity.ok(ApiResponse.onSuccess(dtos));
    }

    @Operation(summary = "채팅방 미확인 메시지 개수 및 최신 채팅 Long Polling",
            description = "채팅방 리스트의 미확인 메시지 개수 및 최신 채팅 메시지를 Long Polling 기법으로 가져오는 API 입니다.")
    @GetMapping("/refresh")
    public DeferredResult<ResponseEntity<ApiResponse<List<ChatRoomResponseDto.ChatRoomListDto>>>> chatRoomPolling() {
        DeferredResult<ResponseEntity<ApiResponse<List<ChatRoomResponseDto.ChatRoomListDto>>>> deferredResult = new DeferredResult<>(30000L); // 타임아웃 30초
        chatRoomInfoQueryService.registerPolling(deferredResult);
        return deferredResult;
    }

    @Operation(summary = "채팅방 접속하기",
            description = "채팅방에 접속해서 읽지 않은 새로운 채팅을 읽는 API 입니다.")
    @PatchMapping("/join")
    public ResponseEntity<ApiResponse<Void>> joinChatRoom(@RequestBody ChatRoomRequestDto.ChatRoomJoinDto chatRoomJoinDto) {
        chatRoomInfoCommandService.joinChatRoom(chatRoomJoinDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

    @Operation(summary = "채팅방 썸네일 이미지 변경하기",
            description = "사용자가 채팅방 썸네일 이미지를 변경하는 API 입니다.")
    @PatchMapping("/thumbnails")
    public ResponseEntity<ApiResponse<Void>> updateChatRoomThumbnail(@RequestBody ChatRoomRequestDto.ChatRoomThumbNailDto chatRoomThumbNailDto) {
        chatRoomInfoCommandService.updateThumbnail(chatRoomThumbNailDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }
}
