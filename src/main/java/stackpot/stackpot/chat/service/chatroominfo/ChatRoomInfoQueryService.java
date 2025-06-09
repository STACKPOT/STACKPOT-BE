package stackpot.stackpot.chat.service.chatroominfo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.PotHandler;
import stackpot.stackpot.chat.entity.ChatRoomInfo;
import stackpot.stackpot.chat.repository.ChatRoomInfoRepository;

@Service
@RequiredArgsConstructor
public class ChatRoomInfoQueryService {

    private final ChatRoomInfoRepository chatRoomInfoRepository;

    public Long selectLastReadChatIdByPotMemberIdAndChatRoomId(Long potMemberId, Long chatRoomId) {
        return chatRoomInfoRepository.selectLastReadChatIdByPotMemberIdAndChatRoomId(potMemberId, chatRoomId).orElse(null);
    }

    public ChatRoomInfo selectChatRoomInfoByPotMemberIdAndChatRoomId(Long potMemberId, Long chatRoomId) {
        return chatRoomInfoRepository.selectChatRoomInfoByPotMemberIdAndChatRoomId(potMemberId, chatRoomId).orElseThrow(() -> new PotHandler(ErrorStatus.CHATROOM_NOT_FOUND));
    }

    public String selectThumbnailUrlByPotMemberIdAndChatRoomId(Long potMemberId, Long chatRoomId) {
        return chatRoomInfoRepository.selectThumbnailUrlByPotMemberIdAndChatRoomId(potMemberId, chatRoomId).orElse(null);
    }
}
