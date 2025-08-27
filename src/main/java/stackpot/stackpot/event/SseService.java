package stackpot.stackpot.event;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import stackpot.stackpot.chat.dto.ChatDto;
import stackpot.stackpot.chat.dto.ChatRoomDto;
import stackpot.stackpot.chat.dto.response.ChatRoomResponseDto;
import stackpot.stackpot.chat.service.chat.ChatQueryService;
import stackpot.stackpot.chat.service.chatroom.ChatRoomQueryService;
import stackpot.stackpot.chat.service.chatroominfo.ChatRoomInfoQueryService;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.notification.event.*;
import stackpot.stackpot.pot.dto.UserMemberIdDto;
import stackpot.stackpot.pot.service.potMember.PotMemberQueryService;

import java.io.IOException;
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

    // user 마다 SseEmitter 객체 저장
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
            log.error("SSE 연결 중 에러 발생: {}", e.getMessage());
            emitters.remove(userId);
        }
        return emitter;
    }

    @Scheduled(fixedRate = 30000) // 30초마다 실행
    public void sendHeartbeat() {
        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("heartbeat")
                        .data("keep-alive at " + System.currentTimeMillis()));
            } catch (IOException e) {
                log.warn("Failed to send heartbeat to user {}, removing emitter.", userId);
                emitters.remove(userId);
            }
        });
    }

    public void sendChatRoomList(Long chatRoomId) {
        Long potId = chatRoomQueryService.selectPotIdByChatRoomId(chatRoomId);
        List<Long> allUserIds = potMemberQueryService.selectUserIdsAboutPotMembersByPotId(potId); // 채팅방에 있는 모든 사용자 userId

        for (Long userId : allUserIds) {
            SseEmitter emitter = userId != null ? emitters.get(userId) : null;
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

    /**
     * PotApplicationNotification
     * 팟 만든 사람에게 팟 지원 실시간 알림 전송
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendPotApplicationNotification(PotApplicationEvent event) {
        SseEmitter emitter = event.getPotLeaderId() != null ? emitters.get(event.getPotLeaderId()) : null;
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("Notification")
                        .data(event.getUnReadNotificationDto(), MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                emitters.remove(event.getPotLeaderId());
            }
        }
    }

    /**
     * PotCommentNotification
     * 팟을 만든 사람과 부모 댓글 작성자에게 실시간 알림 전송
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendPotCommentNotification(PotCommentEvent event) {
        SseEmitter emitter = event.getPotLeaderId() != null ? emitters.get(event.getPotLeaderId()) : null;
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("Notification")
                        .data(event.getUnReadNotificationDto(), MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                emitters.remove(event.getPotLeaderId());
            }
        }
        SseEmitter parentCommentEmitter = event.getParentCommentWriterId() != null ? emitters.get(event.getParentCommentWriterId()) : null;
        if (emitter != parentCommentEmitter && parentCommentEmitter != null) {
            try {
                parentCommentEmitter.send(SseEmitter.event()
                        .name("Notification")
                        .data(event.getUnReadNotificationDto(), MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                emitters.remove(event.getParentCommentWriterId());
            }
        }
    }

    /**
     * FeedCommentNotification
     * 피드 작성자와 부모 댓글 작성자에게 실시간 알림 전송
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendFeedCommentNotification(FeedCommentEvent event) {
        SseEmitter emitter = event.getFeedWriterId() != null ? emitters.get(event.getFeedWriterId()) : null;
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("Notification")
                        .data(event.getUnReadNotificationDto(), MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                emitters.remove(event.getFeedWriterId());
            }
        }
        SseEmitter parentCommentEmitter = event.getParentCommentWriterId() != null ? emitters.get(event.getParentCommentWriterId()) : null;
        if (emitter != parentCommentEmitter && parentCommentEmitter != null) {
            try {
                parentCommentEmitter.send(SseEmitter.event()
                        .name("Notification")
                        .data(event.getUnReadNotificationDto(), MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                emitters.remove(event.getParentCommentWriterId());
            }
        }
    }

    /**
     * FeedLikeNotification
     * 피드 작성자에게 실시간 알림 전송
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendFeedLikeNotification(FeedLikeEvent event) {
        SseEmitter emitter = event.getFeedWriterId() != null ? emitters.get(event.getFeedWriterId()) : null;
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("Notification")
                        .data(event.getUnReadNotificationDto(), MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                emitters.remove(event.getFeedWriterId());
            }
        }
    }

    /**
     * PotEndNotification
     * 팟을 끓인 후 팟 멤버들에게 실시간 알림 전송
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendPotEndNotification(PotEndEvent event) {
        Long potId = event.getPotId();
        List<Long> userIds = potMemberQueryService.selectUserIdsAboutPotMembersByPotId(potId);
        for (Long userId : userIds) {
            SseEmitter emitter = userId != null ? emitters.get(userId) : null;
            if (emitter != null) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("Notification")
                            .data(event.getUnReadNotificationDto(), MediaType.APPLICATION_JSON));
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
