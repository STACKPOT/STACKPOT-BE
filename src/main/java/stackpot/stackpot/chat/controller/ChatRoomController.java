package stackpot.stackpot.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.chat.dto.request.ChatRoomRequestDto;
import stackpot.stackpot.chat.dto.response.ChatRoomResponseDto;
import stackpot.stackpot.chat.facade.ChatRoomFacade;
import stackpot.stackpot.common.swagger.ApiErrorCodeExamples;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Chat Management", description = "채팅방 API")
@RequestMapping("/chat-rooms")
public class ChatRoomController {

    private final ChatRoomFacade chatRoomFacade;

    @Operation(summary = "채팅방 목록 가져오기",
            description = "사용자가 속한 모든 채팅방 목록을 가져오는 API 입니다.")
    @ApiErrorCodeExamples({
            ErrorStatus.AUTHENTICATION_FAILED,
            ErrorStatus.POT_MEMBER_NOT_FOUND,
            ErrorStatus.CHATROOM_NOT_FOUND
    })
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<ChatRoomResponseDto.ChatRoomListDto>>> getChatRooms() {
        List<ChatRoomResponseDto.ChatRoomListDto> dtos = chatRoomFacade.selectChatRoomList();
        return ResponseEntity.ok(ApiResponse.onSuccess(dtos));
    }

    @Operation(summary = "채팅방 접속하기",
            description = "채팅방에 접속해서 읽지 않은 새로운 채팅을 읽는 API 입니다.")
    @ApiErrorCodeExamples({
            ErrorStatus.AUTHENTICATION_FAILED,
            ErrorStatus.CHATROOM_NOT_FOUND,
            ErrorStatus.POT_MEMBER_NOT_FOUND,
    })
    @PatchMapping("/join")
    public ResponseEntity<ApiResponse<Void>> joinChatRoom(@RequestBody ChatRoomRequestDto.ChatRoomJoinDto chatRoomJoinDto) {
        chatRoomFacade.joinChatRoom(chatRoomJoinDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

    @Operation(summary = "채팅방 썸네일 이미지 변경하기",
            description = "사용자가 채팅방 썸네일 이미지를 변경하는 API 입니다.")
    @ApiErrorCodeExamples({
            ErrorStatus.AUTHENTICATION_FAILED,
            ErrorStatus.CHATROOM_NOT_FOUND,
            ErrorStatus.POT_MEMBER_NOT_FOUND
    })
    @PatchMapping(value = "/{chatRoomId}/thumbnails", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> updateChatRoomThumbnail(@PathVariable("chatRoomId") Long chatRoomId,
                                                                     @RequestPart("file") MultipartFile file) {
        chatRoomFacade.updateThumbnail(chatRoomId, file);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }
}
