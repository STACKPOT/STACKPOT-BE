package stackpot.stackpot.chat.service.event;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import stackpot.stackpot.chat.dto.ChatDto;
import stackpot.stackpot.chat.dto.ChatRoomDto;
import stackpot.stackpot.chat.dto.response.ChatRoomResponseDto;
import stackpot.stackpot.chat.service.chat.ChatQueryService;
import stackpot.stackpot.chat.service.chatroom.ChatRoomQueryService;
import stackpot.stackpot.chat.service.chatroominfo.ChatRoomInfoQueryService;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.pot.dto.UserMemberIdDto;
import stackpot.stackpot.pot.service.PotMemberQueryService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Service
@Slf4j
public class SseService {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    private final ChatRoomQueryService chatRoomQueryService;
    private final ChatRoomInfoQueryService chatRoomInfoQueryService;
    private final ChatQueryService chatQueryService;
    private final PotMemberQueryService potMemberQueryService;
    private final AuthService authService;

    public SseEmitter connect() {
        Long userId = authService.getCurrentUserId();
        SseEmitter emitter = new SseEmitter(300000L); // 5분 타임아웃
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitters.put(userId, emitter);
        try {
            emitter.send(SseEmitter.event().name("connect").data("연결 완료!"));
        } catch (Exception e) {
            emitters.remove(userId);
        }
        return emitter;
    }

    public void sendChatRoomList(Long chatRoomId) {
        Long potId = chatRoomQueryService.selectPotIdByChatRoomId(chatRoomId);
        List<Long> allUserIds = potMemberQueryService.selectUserIdsAboutPotMembersByPotId(potId); // 채팅방에 있는 모든 사용자 userId

        for (Long userId : allUserIds) {
            SseEmitter emitter = emitters.get(userId);
            if (emitter != null) {
                List<UserMemberIdDto> potMemberIds = potMemberQueryService.selectPotMemberIdsByUserId(userId);
                List<ChatRoomResponseDto.ChatRoomListDto> results = new ArrayList<>();
                // 사용자 1명이 속한 모든 채팅방(팟)에 대해 채팅방 정보를 가져온다.
                for (UserMemberIdDto ids : potMemberIds) {
                    results.add(createChatRoomListDto(ids));
                }
                Collections.sort(results); // 특정 기준으로 정렬
                try {
                    emitter.send(SseEmitter.event()
                            .name("chatRoomList")
                            .data(results));
                } catch (Exception e) {
                    emitters.remove(userId);
                }
            }
        }
    }

    private ChatRoomResponseDto.ChatRoomListDto createChatRoomListDto(UserMemberIdDto ids) {
        Long potMemberId = ids.getPotMemberId();
        Long potId = ids.getPotId();

        ChatRoomDto.ChatRoomNameDto chatRoomNameDto = chatRoomQueryService.selectChatRoomNameDtoIdByPotId(potId);
        Long chatRoomId = chatRoomNameDto.getChatRoomId();

        Long lastReadChatId = chatRoomInfoQueryService.selectLastReadChatIdByPotMemberIdAndChatRoomId(potMemberId, chatRoomId);

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
