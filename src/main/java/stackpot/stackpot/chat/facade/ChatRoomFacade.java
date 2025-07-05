package stackpot.stackpot.chat.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import stackpot.stackpot.aws.s3.AmazonS3Manager;
import stackpot.stackpot.chat.converter.ChatConverter;
import stackpot.stackpot.chat.dto.ChatDto;
import stackpot.stackpot.chat.dto.ChatRoomDto;
import stackpot.stackpot.chat.dto.request.ChatRoomRequestDto;
import stackpot.stackpot.chat.dto.response.ChatRoomResponseDto;
import stackpot.stackpot.chat.service.chat.ChatQueryService;
import stackpot.stackpot.chat.service.chatroom.ChatRoomQueryService;
import stackpot.stackpot.chat.service.chatroominfo.ChatRoomInfoCommandService;
import stackpot.stackpot.chat.service.chatroominfo.ChatRoomInfoQueryService;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.pot.dto.UserMemberIdDto;
import stackpot.stackpot.pot.service.potMember.PotMemberQueryService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatRoomFacade {

    private final ChatRoomInfoCommandService chatRoomInfoCommandService;
    private final ChatRoomInfoQueryService chatRoomInfoQueryService;
    private final ChatRoomQueryService chatRoomQueryService;
    private final ChatQueryService chatQueryService;
    private final ChatConverter chatConverter;
    private final PotMemberQueryService potMemberQueryService;
    private final AuthService authService;
    private final AmazonS3Manager amazonS3Manager;

    // 채팅방 목록 조회
    public List<ChatRoomResponseDto.ChatRoomListDto> selectChatRoomList() {
        List<ChatRoomResponseDto.ChatRoomListDto> result = new ArrayList<>();

        Long userId = authService.getCurrentUserId();
        List<UserMemberIdDto> potMemberIds = potMemberQueryService.selectPotMemberIdsByUserId(userId);
        if (potMemberIds.isEmpty()) {
            return result;
        }

        for (UserMemberIdDto ids : potMemberIds) {
            ChatRoomResponseDto.ChatRoomListDto dto = createChatRoomListDto(ids);
            result.add(dto);
        }
        Collections.sort(result);
        return result;
    }

    public void joinChatRoom(ChatRoomRequestDto.ChatRoomJoinDto chatRoomJoinDto) {
        Long userId = authService.getCurrentUserId();
        Long chatRoomId = chatRoomJoinDto.getChatRoomId();
        Long potId = chatRoomQueryService.selectPotIdByChatRoomId(chatRoomId);
        Long potMemberId = potMemberQueryService.selectPotMemberIdByUserIdAndPotId(userId, potId);
        Long latestChatId = chatQueryService.selectLatestChatId(chatRoomId);

        chatRoomInfoCommandService.joinChatRoom(potMemberId, chatRoomId, latestChatId);
    }

    public void updateThumbnail(Long chatRoomId, MultipartFile file) {
        Long userId = authService.getCurrentUserId();
        Long potId = chatRoomQueryService.selectPotIdByChatRoomId(chatRoomId);
        Long potMemberId = potMemberQueryService.selectPotMemberIdByUserIdAndPotId(userId, potId);

        String keyName = "chat-room/" + UUID.randomUUID();
        String imageUrl = amazonS3Manager.uploadFile(keyName, file);

        chatRoomInfoCommandService.updateThumbnail(potMemberId, chatRoomId, imageUrl);
    }

    private ChatRoomResponseDto.ChatRoomListDto createChatRoomListDto(UserMemberIdDto ids) {
        Long potMemberId = ids.getPotMemberId();
        Long potId = ids.getPotId();

        ChatRoomDto.ChatRoomNameDto chatRoomNameDto = chatRoomQueryService.selectChatRoomNameDtoIdByPotId(potId);
        if (chatRoomNameDto == null)
            return null;

        Long chatRoomId = chatRoomNameDto.getChatRoomId();
        Long lastReadChatId = chatRoomInfoQueryService.selectLastReadChatIdByPotMemberIdAndChatRoomId(potMemberId, chatRoomId);
        ChatDto.LastChatDto lastChatDto = chatQueryService.selectLastChatInChatRoom(chatRoomId);

        String thumbnailUrl = chatRoomInfoQueryService.selectThumbnailUrlByPotMemberIdAndChatRoomId(potMemberId, chatRoomId);
        String chatRoomName = chatRoomNameDto.getChatRoomName();
        String lastChat = lastChatDto.getLastChat();
        LocalDateTime lastChatTime = lastChatDto.getLastChatTime();
        int unReadMessageCount = chatQueryService.getUnReadMessageCount(chatRoomId, lastReadChatId);

        return chatConverter.toChatRoomListDto(
                chatRoomId,
                chatRoomName,
                thumbnailUrl,
                lastChatTime,
                lastChat,
                unReadMessageCount
        );
    }
}
