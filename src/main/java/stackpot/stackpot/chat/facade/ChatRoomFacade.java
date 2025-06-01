package stackpot.stackpot.chat.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.aws.s3.AmazonS3Manager;
import stackpot.stackpot.chat.dto.ChatDto;
import stackpot.stackpot.chat.dto.ChatRoomDto;
import stackpot.stackpot.chat.dto.request.ChatRoomRequestDto;
import stackpot.stackpot.chat.dto.response.ChatRoomResponseDto;
import stackpot.stackpot.chat.event.NewChatEvent;
import stackpot.stackpot.chat.service.chat.ChatQueryService;
import stackpot.stackpot.chat.service.chatroom.ChatRoomCommandService;
import stackpot.stackpot.chat.service.chatroom.ChatRoomQueryService;
import stackpot.stackpot.chat.service.chatroominfo.ChatRoomInfoCommandService;
import stackpot.stackpot.chat.service.chatroominfo.ChatRoomInfoQueryService;
import stackpot.stackpot.chat.session.ChatSessionManager;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.pot.dto.UserMemberIdDto;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.service.PotMemberQueryService;
import stackpot.stackpot.pot.service.PotQueryService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ChatRoomFacade {

    private final ChatRoomInfoCommandService chatRoomInfoCommandService;
    private final ChatRoomCommandService chatRoomCommandService;
    private final ChatRoomInfoQueryService chatRoomInfoQueryService;
    private final ChatRoomQueryService chatRoomQueryService;
    private final ChatQueryService chatQueryService;
    private final PotMemberQueryService potMemberQueryService;
    private final PotQueryService potQueryService;
    private final AuthService authService;
    private final AmazonS3Manager amazonS3Manager;

    private final Map<Long, List<DeferredResult<ResponseEntity<ApiResponse<List<ChatRoomResponseDto.ChatRoomListDto>>>>>> waitingQueue = new ConcurrentHashMap<>();

    // OSIV 끄면 pot 준영속이라서 안 됨
    public void createChatRoom(ChatRoomRequestDto.CreateChatRoomDto createChatRoomDto) {
        Long potId = createChatRoomDto.getPotId();
        Pot pot = potQueryService.getPotByPotId(potId);
        chatRoomCommandService.createChatRoom(pot.getPotName(), pot);
    }

    public void createChatRoomInfo(ChatRoomRequestDto.CreateChatRoomInfoDto createChatRoomInfoDto) {
        List<Long> potMemberIds = createChatRoomInfoDto.getPotMemberIds();
        Long potId = createChatRoomInfoDto.getPotId();
        Long chatRoomId = chatRoomQueryService.selectChatRoomIdByPotId(potId);
        chatRoomInfoCommandService.createChatRoomInfo(potMemberIds, chatRoomId);
    }

    public List<ChatRoomResponseDto.ChatRoomListDto> selectChatRoomList() {
        List<ChatRoomResponseDto.ChatRoomListDto> result = new ArrayList<>();

        Long userId = authService.getCurrentUserId();
        List<UserMemberIdDto> potMemberIds = potMemberQueryService.selectPotMemberIdsByUserId(userId);

        for (UserMemberIdDto ids : potMemberIds) {
            ChatRoomResponseDto.ChatRoomListDto dto = createChatRoomListDto(ids);
            result.add(dto);
        }
        return result;
    }

    public void registerPolling(DeferredResult<ResponseEntity<ApiResponse<List<ChatRoomResponseDto.ChatRoomListDto>>>> deferredResult) {
        Long userId = authService.getCurrentUserId();
        waitingQueue.computeIfAbsent(userId, k -> new ArrayList<>()).add(deferredResult);
        deferredResult.onTimeout(() -> {
            waitingQueue.get(userId).remove(deferredResult);
            deferredResult.setResult(ResponseEntity.ok(ApiResponse.onSuccess(null)));
        });
        deferredResult.onCompletion(() -> waitingQueue.get(userId).remove(deferredResult));
    }

    public void joinChatRoom(ChatRoomRequestDto.ChatRoomJoinDto chatRoomJoinDto) {
        Long userId = authService.getCurrentUserId();
        Long chatRoomId = chatRoomJoinDto.getChatRoomId();
        Long potId = chatRoomQueryService.selectPotIdByChatRoomId(chatRoomId);
        Long potMemberId = potMemberQueryService.selectPotMemberIdByUserIdAndPotId(userId, potId);
        String latestChatId = chatQueryService.selectLatestChatId(chatRoomId);

        chatRoomInfoCommandService.joinChatRoom(potMemberId, chatRoomId, latestChatId);
    }

    public void updateThumbnail(ChatRoomRequestDto.ChatRoomThumbNailDto chatRoomThumbNailDto) {
        Long chatRoomId = chatRoomThumbNailDto.getChatRoomId();
        Long potId = chatRoomQueryService.selectPotIdByChatRoomId(chatRoomId);
        Long userId = authService.getCurrentUserId();
        Long potMemberId = potMemberQueryService.selectPotMemberIdByUserIdAndPotId(userId, potId);

        String keyName = "chat-room/" + UUID.randomUUID();
        String imageUrl = amazonS3Manager.uploadFile(keyName, chatRoomThumbNailDto.getFile());

        chatRoomInfoCommandService.updateThumbnail(potMemberId, chatRoomId, imageUrl);
    }

    @EventListener
    public void handlePollingEvent(NewChatEvent event) {
        Long chatRoomId = event.getChatRoomId();
        Long potId = chatRoomQueryService.selectPotIdByChatRoomId(chatRoomId);
        List<Long> allUserIds = potMemberQueryService.selectUserIdsAboutPotMembersByPotId(potId); // 채팅방에 있는 모든 사용자 userId

        for (Long userId : allUserIds) {
            List<DeferredResult<ResponseEntity<ApiResponse<List<ChatRoomResponseDto.ChatRoomListDto>>>>> deferredResults = waitingQueue.get(userId);
            if (deferredResults == null || deferredResults.isEmpty())
                continue;

            List<UserMemberIdDto> potMemberIds = potMemberQueryService.selectPotMemberIdsByUserId(userId);
            List<ChatRoomResponseDto.ChatRoomListDto> results = new ArrayList<>();

            // 사용자 1명이 속한 모든 채팅방(팟)에 대해 채팅방 정보를 가져온다.
            for (UserMemberIdDto ids : potMemberIds) {
                results.add(createChatRoomListDto(ids));
            }

            for (DeferredResult<ResponseEntity<ApiResponse<List<ChatRoomResponseDto.ChatRoomListDto>>>> deferredResult : deferredResults) {
                deferredResult.setResult(ResponseEntity.ok(ApiResponse.onSuccess(results)));
            }
            deferredResults.clear();
        }
    }

    private ChatRoomResponseDto.ChatRoomListDto createChatRoomListDto(UserMemberIdDto ids) {
        Long potMemberId = ids.getPotMemberId();
        Long potId = ids.getPotId();

        ChatRoomDto.ChatRoomNameDto chatRoomNameDto = chatRoomQueryService.selectChatRoomNameDtoIdByPotId(potId);
        Long chatRoomId = chatRoomNameDto.getChatRoomId();

        String lastReadChatId = chatRoomInfoQueryService.selectLastReadChatIdByPotMemberIdAndChatRoomId(potMemberId, chatRoomId);

        ChatDto.LastChatDto lastChatDto = chatQueryService.selectLastChatInChatRoom(chatRoomId);

        String thumbnailUrl = chatRoomInfoQueryService.selectThumbnailUrlByPotMemberIdAndChatRoomId(potMemberId, chatRoomId);
        String chatRoomName = chatRoomNameDto.getChatRoomName();
        String lastChat = lastChatDto.getLastChat();
        LocalDateTime lastChatTime = lastChatDto.getLastChatTime();
        int unReadMessageCount = chatQueryService.getUnReadMessageCount(chatRoomId, lastReadChatId);

        return ChatRoomResponseDto.ChatRoomListDto.builder()
                .chatRoomId(chatRoomId)
                .chatRoomName(chatRoomName)
                .thumbnailUrl(thumbnailUrl)
                .lastChatTime(lastChatTime)
                .lastChat(lastChat)
                .unReadMessageCount(unReadMessageCount)
                .build();
    }
}
