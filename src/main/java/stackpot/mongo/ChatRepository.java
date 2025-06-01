package stackpot.mongo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends MongoRepository<Chat, String> {

    Optional<ChatId> findFirstChatIdByChatRoomIdOrderByIdDesc(Long chatRoomId);

    Optional<Chat> findFirstByChatRoomIdOrderByIdDesc(Long chatRoomId);

    List<Chat> findByChatRoomIdAndIdGreaterThanOrderByIdAsc(Long chatRoomId, String id, Pageable pageable);

    List<Chat> findByChatRoomIdAndIdLessThanOrderByIdDesc(Long chatRoomId, String id, Pageable pageable);

    int countByChatRoomIdAndIdGreaterThan(Long chatRoomId, String lastReadChatId);
}
