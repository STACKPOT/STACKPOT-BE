package stackpot.stackpot.chat.service.chatroominfo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stackpot.stackpot.chat.entity.ChatRoomInfo;
import stackpot.stackpot.chat.repository.ChatRoomInfoBatchRepository;
import stackpot.stackpot.chat.repository.ChatRoomInfoRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomInfoCommandService {

    private final ChatRoomInfoQueryService chatRoomInfoQueryService;
    private final ChatRoomInfoRepository chatRoomInfoRepository;
    private final ChatRoomInfoBatchRepository chatRoomInfoBatchRepository;

    public void createChatRoomInfo(List<Long> potMembers, Long chatRoomId) {
        chatRoomInfoBatchRepository.chatRoomInfoBatchInsert(potMembers, chatRoomId);
    }

    @Transactional
    public void joinChatRoom(Long potMemberId, Long chatRoomId, Long latestChatId) {
        ChatRoomInfo chatRoomInfo = chatRoomInfoQueryService.selectChatRoomInfoByPotMemberIdAndChatRoomId(potMemberId, chatRoomId);
        chatRoomInfo.updateLastReadChatId(latestChatId);
    }

    public void updateLastReadChatId(List<Long> potMemberIds, Long chatRoomId, Long chatId) {
        chatRoomInfoBatchRepository.lastReadChatIdBatchUpdate(potMemberIds, chatRoomId, chatId);
    }

    @Transactional
    public void updateThumbnail(Long potMemberId, Long chatRoomId, String imageUrl) {
        ChatRoomInfo chatRoomInfo = chatRoomInfoQueryService.selectChatRoomInfoByPotMemberIdAndChatRoomId(potMemberId, chatRoomId);
        chatRoomInfo.updateThumbnail(imageUrl);
    }

    @Transactional
    public void deleteChatRoomInfo(Long potMemberId, Long chatRoomId) {
        chatRoomInfoRepository.deleteByPotMemberIdAndChatRoomId(potMemberId, chatRoomId);
    }

    @Transactional
    public void deleteChatRoomInfo(List<Long> potMemberIds) {
        chatRoomInfoRepository.deleteByPotMemberIdIn(potMemberIds);
    }
}
