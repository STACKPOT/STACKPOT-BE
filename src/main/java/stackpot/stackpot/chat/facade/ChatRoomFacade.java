package stackpot.stackpot.chat.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import stackpot.stackpot.aws.s3.AmazonS3Manager;
import stackpot.stackpot.chat.dto.ChatDto;
import stackpot.stackpot.chat.dto.ChatRoomDto;
import stackpot.stackpot.chat.dto.request.ChatRoomRequestDto;
import stackpot.stackpot.chat.dto.response.ChatRoomResponseDto;
import stackpot.stackpot.chat.service.chat.ChatQueryService;
import stackpot.stackpot.chat.service.chatroom.ChatRoomCommandService;
import stackpot.stackpot.chat.service.chatroom.ChatRoomQueryService;
import stackpot.stackpot.chat.service.chatroominfo.ChatRoomInfoCommandService;
import stackpot.stackpot.chat.service.chatroominfo.ChatRoomInfoQueryService;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.pot.dto.UserMemberIdDto;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.service.potMember.PotMemberQueryService;
import stackpot.stackpot.pot.service.pot.PotQueryService;

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
    private final ChatRoomCommandService chatRoomCommandService;
    private final ChatRoomInfoQueryService chatRoomInfoQueryService;
    private final ChatRoomQueryService chatRoomQueryService;
    private final ChatQueryService chatQueryService;
    private final PotMemberQueryService potMemberQueryService;
    private final PotQueryService potQueryService;
    private final AuthService authService;
    private final AmazonS3Manager amazonS3Manager;

    // OSIV 끄면 pot 준영속이라서 안 됨
    // 채팅방 생성
    public void createChatRoom(ChatRoomRequestDto.CreateChatRoomDto createChatRoomDto) {
        Long potId = createChatRoomDto.getPotId();
        Pot pot = potQueryService.getPotByPotId(potId);
        chatRoomCommandService.createChatRoom(pot.getPotName(), pot);
    }

    // 채팅방 정보 생성
    public void createChatRoomInfo(ChatRoomRequestDto.CreateChatRoomInfoDto createChatRoomInfoDto) {
        List<Long> potMemberIds = createChatRoomInfoDto.getPotMemberIds();
        Long potId = createChatRoomInfoDto.getPotId();
        Long chatRoomId = chatRoomQueryService.selectChatRoomIdByPotId(potId);
        chatRoomInfoCommandService.createChatRoomInfo(potMemberIds, chatRoomId);
    }

    // 채팅방 목록 조회
    public List<ChatRoomResponseDto.ChatRoomListDto> selectChatRoomList() {
        List<ChatRoomResponseDto.ChatRoomListDto> result = new ArrayList<>();

        Long userId = authService.getCurrentUserId();
        List<UserMemberIdDto> potMemberIds = potMemberQueryService.selectPotMemberIdsByUserId(userId);

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
        Long potId = chatRoomQueryService.selectPotIdByChatRoomId(chatRoomId);
        Long userId = authService.getCurrentUserId();
        Long potMemberId = potMemberQueryService.selectPotMemberIdByUserIdAndPotId(userId, potId);

        String keyName = "chat-room/" + UUID.randomUUID();
        String imageUrl = amazonS3Manager.uploadFile(keyName, file);

        chatRoomInfoCommandService.updateThumbnail(potMemberId, chatRoomId, imageUrl);
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
