package stackpot.stackpot.chat.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import stackpot.stackpot.chat.dto.ChatRoomDto;
import stackpot.stackpot.chat.entity.ChatRoom;
import stackpot.stackpot.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("select cr.pot.potId from ChatRoom cr where cr.id = :chatRoomId")
    Optional<Long> findPotIdByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    @Query("select new stackpot.stackpot.chat.dto.ChatRoomDto$ChatRoomNameDto(cr.id, cr.chatRoomName) from ChatRoom cr where cr.pot.potId = :potId")
    Optional<ChatRoomDto.ChatRoomNameDto> findChatRoomNameDtoIdByPotId(@Param("potId") Long potId);

    @Query("select cr.id from ChatRoom cr where cr.pot.potId = :potId")
    Optional<Long> selectChatRoomIdByPotId(@Param("potId") Long potId);

    @Query("select cr from ChatRoom cr where cr.pot.potId = :potId")
    Optional<ChatRoom> selectChatRoomByPotId(@Param("potId") Long potId);

    @Modifying
    @Query("delete from ChatRoom c where c.pot.potId = :potId")
    void deleteByPotId(@Param("potId") Long potId);



    @Query("select cr.id from ChatRoom cr where cr.pot.potId in :potIds")
    List<Long> findIdsByPotIdIn(@Param("potIds") List<Long> potIds);


}
