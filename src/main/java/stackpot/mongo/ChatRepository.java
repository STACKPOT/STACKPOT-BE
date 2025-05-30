package stackpot.mongo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.chat.entity.Chat;

import java.util.List;

@Repository
public interface ChatRepository extends MongoRepository<Chat, String> {

    ChatId findChatIdFirstByChatRoomIdOrderByIdDesc(Long chatRoomId);

    Chat findChatFirstByChatRoomIdOrderByIdDesc(Long chatRoomId);

    List<Chat> findByChatRoomIdAndIdGreaterThanOrderByIdAsc(Long chatRoomId, String id, Pageable pageable);

    List<Chat> findByChatRoomIdAndIdLessThanOrderByIdDesc(Long chatRoomId, String id, Pageable pageable);

    int countByChatRoomIdAndIdGreaterThan(Long chatRoomId, String lastReadChatId);
}
