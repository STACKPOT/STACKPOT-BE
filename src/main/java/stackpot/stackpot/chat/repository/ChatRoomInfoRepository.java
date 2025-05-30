package stackpot.stackpot.chat.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import stackpot.stackpot.chat.entity.ChatRoomInfo;

import java.util.Optional;

public interface ChatRoomInfoRepository extends JpaRepository<ChatRoomInfo, Long> {

    @Query("select cri.lastReadChatId from ChatRoomInfo cri where cri.potMember.potMemberId = :potMemberId and cri.chatRoom.id = :chatRoomId")
    Optional<String> selectLastReadChatIdByPotMemberIdAndChatRoomId(@Param("potMemberId") Long potMemberId, @Param("chatRoomId") Long chatRoomId);

    @Query("select cri.imageUrl from ChatRoomInfo cri where cri.potMember.potMemberId = :potMemberId and cri.chatRoom.id = :chatRoomId")
    Optional<String> selectThumbnailUrlByPotMemberIdAndChatRoomId(@Param("potMemberId") Long potMemberId, @Param("chatRoomId") Long chatRoomId);

    @Query("select cri from ChatRoomInfo cri where cri.potMember.potMemberId = :potMemberId and cri.chatRoom.id = :chatRoomId")
    Optional<ChatRoomInfo> selectChatRoomInfoByPotMemberIdAndChatRoomId(@Param("potMemberId") Long potMemberId, @Param("chatRoomId") Long chatRoomId);
}
