package stackpot.stackpot.chat.service.chatroominfo;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.chat.dto.ChatDto;
import stackpot.stackpot.chat.dto.ChatRoomDto;
import stackpot.stackpot.chat.dto.response.ChatRoomResponseDto;
import stackpot.stackpot.chat.event.NewChatEvent;
import stackpot.stackpot.chat.repository.ChatRoomInfoRepository;
import stackpot.stackpot.chat.service.chat.ChatQueryService;
import stackpot.stackpot.chat.service.chatroom.ChatRoomQueryService;
import stackpot.stackpot.chat.session.ChatSessionManager;
import stackpot.stackpot.pot.dto.UserMemberIdDto;
import stackpot.stackpot.pot.service.PotMemberQueryService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ChatRoomInfoQueryServiceImpl implements ChatRoomInfoQueryService {

    private final ChatRoomQueryService chatRoomQueryService;
    private final ChatQueryService chatQueryService;
    private final PotMemberQueryService potMemberQueryService;
    private final ChatRoomInfoRepository chatRoomInfoRepository;
    private final ChatSessionManager chatSessionManager;
    private final Map<Long, List<DeferredResult<ResponseEntity<ApiResponse<List<ChatRoomResponseDto.ChatRoomListDto>>>>>> waitingQueue = new ConcurrentHashMap<>();

    @Override
    public void registerPolling(DeferredResult<ResponseEntity<ApiResponse<List<ChatRoomResponseDto.ChatRoomListDto>>>> deferredResult) {
        // JWT로 userId 가져오기
        Long userId = 1L;

        waitingQueue.computeIfAbsent(userId, k -> new ArrayList<>()).add(deferredResult);
        deferredResult.onTimeout(() -> {
            waitingQueue.get(userId).remove(deferredResult);
            deferredResult.setResult(ResponseEntity.ok(ApiResponse.onSuccess(null)));
        });
        deferredResult.onCompletion(() -> waitingQueue.get(userId).remove(deferredResult));
    }

    @Override
    public String selectLastReadChatIdByPotMemberIdAndChatRoomId(Long potMemberId, Long chatRoomId) {
        return chatRoomInfoRepository.selectLastReadChatIdByPotMemberIdAndChatRoomId(potMemberId, chatRoomId).orElse(null);
    }

    @EventListener
    public void handlePollingEvent(NewChatEvent event) {
        Long chatRoomId = event.getChatRoomId();
        Long potId = chatRoomQueryService.selectPotIdByChatRoomId(chatRoomId);
        List<Long> allUserIds = potMemberQueryService.selectUserIdsAboutPotMembersByPotId(potId);
        List<Long> onlineUserIds = chatSessionManager.getOnlineUserIds(chatRoomId);
        List<Long> offlineUserIds = getOfflineUserIds(allUserIds, onlineUserIds);

        for (Long userId : offlineUserIds) {
            List<DeferredResult<ResponseEntity<ApiResponse<List<ChatRoomResponseDto.ChatRoomListDto>>>>> deferredResults = waitingQueue.get(userId);
            if (deferredResults == null || deferredResults.isEmpty())
                continue;

            List<UserMemberIdDto> potMemberIds = potMemberQueryService.selectPotMemberIdsByUserId(userId);
            List<ChatRoomResponseDto.ChatRoomListDto> results = new ArrayList<>();

            for (UserMemberIdDto ids : potMemberIds) {
                results.add(createChatRoomListDto(ids));
            }

            for (DeferredResult<ResponseEntity<ApiResponse<List<ChatRoomResponseDto.ChatRoomListDto>>>> deferredResult : deferredResults) {
                deferredResult.setResult(ResponseEntity.ok(ApiResponse.onSuccess(results)));
            }
            deferredResults.clear();
        }
    }

    private List<Long> getOfflineUserIds(List<Long> allUserIds, List<Long> onlineUserIds) {
        Set<Long> allSet = new HashSet<>(allUserIds);
        Set<Long> onlineSet = new HashSet<>(onlineUserIds);
        allSet.removeAll(onlineSet);
        return new ArrayList<>(allSet);
    }

    private ChatRoomResponseDto.ChatRoomListDto createChatRoomListDto(UserMemberIdDto ids) {
        Long potMemberId = ids.getPotMemberId();
        Long potId = ids.getPotId();

        ChatRoomDto.ChatRoomNameDto chatRoomNameDto = chatRoomQueryService.selectChatRoomIdByPotId(potId);
        Long chatRoomId = chatRoomNameDto.getChatRoomId();

        String lastReadChatId = selectLastReadChatIdByPotMemberIdAndChatRoomId(potMemberId, chatRoomId);

        ChatDto.LastChatDto lastChatDto = chatQueryService.selectLastChatInChatRoom(chatRoomId);

        String chatRoomName = chatRoomNameDto.getChatRoomName();
        String lastChat = lastChatDto.getLastChat();
        LocalDateTime lastChatTime = lastChatDto.getLastChatTime();
        int unReadMessageCount = chatQueryService.getUnReadMessageCount(chatRoomId, lastReadChatId);

        return ChatRoomResponseDto.ChatRoomListDto.builder()
                .chatRoomName(chatRoomName)
                .lastChatTime(lastChatTime)
                .lastChat(lastChat)
                .unReadMessageCount(unReadMessageCount)
                .build();
    }
}
