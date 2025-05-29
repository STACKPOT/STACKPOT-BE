package stackpot.stackpot.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.chat.dto.request.ChatRequestDto;
import stackpot.stackpot.chat.dto.response.ChatResponseDto;
import stackpot.stackpot.chat.entity.Chat;
import stackpot.stackpot.chat.service.chat.ChatCommandService;
import stackpot.stackpot.chat.service.chat.ChatFileService;
import stackpot.stackpot.chat.service.chat.ChatQueryService;
import stackpot.stackpot.chat.service.chat.ChatSendService;
import stackpot.stackpot.chat.service.chatroominfo.ChatRoomInfoCommandService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Chat Management", description = "채팅 API")
@RequestMapping("/chats")
public class ChatController {

    private final ChatSendService chatSendService;
    private final ChatCommandService chatCommandService;
    private final ChatQueryService chatQueryService;
    private final ChatRoomInfoCommandService chatRoomInfoCommandService;
    private final ChatFileService chatFileService;

    @Operation(summary = "채팅 전송 Publish",
            description = "웹소켓 & Spring 내장 메시지 브로커 Pub/Sub를 통한 실시간 채팅"
    )
    @MessageMapping("/chat/{chatRoomId}")
    public void chat(
            ChatRequestDto.ChatMessageDto chatMessageDto,
            @DestinationVariable(value = "chatRoomId") Long chatRoomId,
            Message<?> message
    ) {
        // JWT를 어떻게 해서 userId, userName을 가져와야 함.
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        Long userId = (Long) accessor.getSessionAttributes().get("userId");

        Chat chat = chatCommandService.saveChatMessage(chatMessageDto);
        chatSendService.sendMessage(chat, chatRoomId);
        chatRoomInfoCommandService.updateLastReadChatId(userId, chatRoomId, chat.getId());
    }

    @Operation(summary = "채팅방의 모든 채팅 가져오기",
            description = "특정 채팅방에서 발생한 모든 채팅 기록을 가져오는 API 입니다.\n")
    @GetMapping("/")
    public ResponseEntity<ApiResponse<ChatResponseDto.AllChatDto>> getAllChatsInChatRoom(
            @RequestParam(name = "chatRoomId") Long chatRoomId,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(name = "direction") String direction) {
        ChatResponseDto.AllChatDto allChatDto = chatQueryService.selectAllChatsInChatRoom(chatRoomId, cursor, size, direction);
        return ResponseEntity.ok(ApiResponse.onSuccess(allChatDto));
    }

    @Operation(summary = "채팅칠 때 파일/이미지 전송 API",
            description = "채팅칠 때 파일이나 이미지를 전송하는 API입니다.\n" +
                    "파일을 전송하면 S3에 저장하고 URL을 반환합니다.")
    @PostMapping("/files")
    public ResponseEntity<ApiResponse<ChatResponseDto.ChatFileDto>> sendFileWhenChat(@RequestPart("file") MultipartFile file) {
        ChatResponseDto.ChatFileDto chatFileDto = chatFileService.saveFileInS3(file);
        return ResponseEntity.ok(ApiResponse.onSuccess(chatFileDto));
    }
}
