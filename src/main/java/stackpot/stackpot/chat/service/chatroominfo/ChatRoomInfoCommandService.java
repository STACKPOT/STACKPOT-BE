package stackpot.stackpot.chat.service.chatroominfo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stackpot.stackpot.chat.entity.ChatRoomInfo;
import stackpot.stackpot.chat.repository.ChatRoomInfoBatchRepository;
import stackpot.stackpot.chat.repository.ChatRoomInfoRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomInfoCommandService {

    private final ChatRoomInfoQueryService chatRoomInfoQueryService;
    private final ChatRoomInfoBatchRepository chatRoomInfoBatchRepository;
    private final ChatRoomInfoRepository chatRoomInfoRepository;

    public void createChatRoomInfo(List<Long> potMembers, Long chatRoomId) {
        chatRoomInfoBatchRepository.chatRoomInfoBatchInsert(potMembers, chatRoomId);
    }

    public void joinChatRoom(Long potMemberId, Long chatRoomId, String latestChatId) {
        ChatRoomInfo chatRoomInfo = chatRoomInfoQueryService.selectChatRoomInfoByPotMemberIdAndChatRoomId(potMemberId, chatRoomId);
        chatRoomInfo.updateLastReadChatId(latestChatId);
        chatRoomInfoRepository.save(chatRoomInfo);
    }

    public void updateLastReadChatId(List<Long> potMemberIds, Long chatRoomId, String chatId) {
        chatRoomInfoBatchRepository.lastReadChatIdBatchUpdate(potMemberIds, chatRoomId, chatId);
    }

    public void updateThumbnail(Long potMemberId, Long chatRoomId, String imageUrl) {
        ChatRoomInfo chatRoomInfo = chatRoomInfoQueryService.selectChatRoomInfoByPotMemberIdAndChatRoomId(potMemberId, chatRoomId);
        chatRoomInfo.updateThumbnail(imageUrl);
        chatRoomInfoRepository.save(chatRoomInfo);
    }
}
