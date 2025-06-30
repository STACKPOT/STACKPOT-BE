package stackpot.stackpot.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.chat.dto.request.ChatRequestDto;
import stackpot.stackpot.chat.dto.response.ChatResponseDto;
import stackpot.stackpot.chat.facade.ChatFacade;
import stackpot.stackpot.common.swagger.ApiErrorCodeExamples;

@RestController
@RequiredArgsConstructor
@Tag(name = "Chat Management", description = "채팅 API")
@RequestMapping("/chats")
public class ChatController {

    private final ChatFacade chatFacade;

    @Operation(summary = "채팅 전송 Publish",
            description = "웹소켓 & Spring 내장 메시지 브로커 Pub/Sub를 통한 실시간 채팅"
    )
    @MessageMapping("/chat/{chatRoomId}")
    public void chat(
            ChatRequestDto.ChatMessageDto chatMessageDto,
            @DestinationVariable(value = "chatRoomId") Long chatRoomId,
            Message<?> message
    ) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        chatFacade.chat(chatMessageDto, userId, chatRoomId);
    }

    @Operation(summary = "채팅방의 모든 채팅 가져오기",
            description = "특정 채팅방에서 발생한 모든 채팅 기록을 가져오는 API 입니다.\n")
    @ApiErrorCodeExamples({
            ErrorStatus.AUTHENTICATION_FAILED,
            ErrorStatus.CHATROOM_NOT_FOUND,
            ErrorStatus.POT_MEMBER_NOT_FOUND,
            ErrorStatus.CHAT_BAD_REQUEST
    })
    @GetMapping("")
    public ResponseEntity<ApiResponse<ChatResponseDto.AllChatDto>> getAllChatsInChatRoom(
            @RequestParam(name = "chatRoomId") Long chatRoomId,
            @RequestParam(name = "cursor", required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(name = "direction", required = false) String direction) {
        ChatResponseDto.AllChatDto allChatDto = chatFacade.selectAllChatsInChatRoom(chatRoomId, cursor, size, direction);
        return ResponseEntity.ok(ApiResponse.onSuccess(allChatDto));
    }

    @Operation(summary = "채팅칠 때 파일/이미지 전송 API",
            description = "채팅칠 때 파일이나 이미지를 전송하는 API입니다.\n" +
                    "파일을 전송하면 S3에 저장하고 URL을 반환합니다.")
    @ApiErrorCodeExamples({
            ErrorStatus._BAD_REQUEST
    })
    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ChatResponseDto.ChatFileDto>> sendFileWhenChat(@RequestPart("file") MultipartFile file) {
        ChatResponseDto.ChatFileDto chatFileDto = chatFacade.saveFileInS3(file);
        return ResponseEntity.ok(ApiResponse.onSuccess(chatFileDto));
    }
}
