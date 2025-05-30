package stackpot.stackpot.chat.service.chatroominfo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stackpot.stackpot.aws.s3.AmazonS3Manager;
import stackpot.stackpot.chat.dto.request.ChatRoomRequestDto;
import stackpot.stackpot.chat.dto.response.ChatRoomResponseDto;
import stackpot.stackpot.chat.repository.ChatRoomInfoBatchRepository;
import stackpot.stackpot.chat.repository.ChatRoomInfoRepository;
import stackpot.stackpot.chat.service.chat.ChatQueryService;
import stackpot.stackpot.chat.service.chatroom.ChatRoomQueryService;
import stackpot.stackpot.chat.session.ChatSessionManager;
import stackpot.stackpot.pot.service.PotMemberQueryService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatRoomInfoCommandServiceImpl implements ChatRoomInfoCommandService {

    private final ChatRoomInfoQueryService chatRoomInfoQueryService;
    private final ChatRoomQueryService chatRoomQueryService;
    private final ChatQueryService chatQueryService;
    private final PotMemberQueryService potMemberQueryService;

    private final ChatRoomInfoBatchRepository chatRoomInfoBatchRepository;
    private final ChatRoomInfoRepository chatRoomInfoRepository;

    private final ChatSessionManager chatSessionManager;
    private final AmazonS3Manager amazonS3Manager;

    @Override
    public void updateLastReadChatId(Long userId, Long chatRoomId, String chatId) {
        Long potId = chatRoomQueryService.selectPotIdByChatRoomId(chatRoomId);

        List<Long> userIds = chatSessionManager.getOnlineUserIds(chatRoomId);
        List<Long> potMemberIds = potMemberQueryService.selectPotMembersIdsByUserIdsAndPotId(userIds, potId);

        chatRoomInfoBatchRepository.lastReadChatIdBatchUpdate(potMemberIds, chatRoomId, chatId);
    }

    @Override
    public void joinChatRoom(ChatRoomRequestDto.ChatRoomJoinDto chatRoomJoinDto) {
        Long chatRoomId = chatRoomJoinDto.getChatRoomId();
        // chatRoomId로 potId 가져오기
        Long potId = chatRoomQueryService.selectPotIdByChatRoomId(chatRoomId);
        // TODO JWT에서 userId 가져오기, userId와 potId로 potMemberId 가져오기

        // 해당 채팅방의 가장 최신 chatId 가져오기
        String latestChatId = chatQueryService.selectLatestChatId(chatRoomId);

        // potMemberId, chatRoomId, chatId로 lastReadChatId 업데이트하기

    }

    @Override
    public void updateThumbnail(ChatRoomRequestDto.ChatRoomThumbNailDto chatRoomThumbNailDto) {
        Long chatRoomId = chatRoomThumbNailDto.getChatRoomId();

        // TODO JWT에서 userId 가져오기, userId와 potId로 potMemberId 가져오기

        String keyName = "chat-room/" + UUID.randomUUID();
        String imageUrl = amazonS3Manager.uploadFile(keyName, chatRoomThumbNailDto.getFile());

        // potMemberId, chatRoomId로 chatRoomInfo의 imageUrl 업데이트

    }
}
