package stackpot.mongo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends MongoRepository<Chat, String> {

    Optional<ChatId> findFirstChatIdByChatRoomIdOrderByIdDesc(Long chatRoomId);

    Optional<Chat> findFirstByChatRoomIdOrderByIdDesc(Long chatRoomId);

    List<Chat> findByChatRoomIdOrderByIdAsc(Long chatRoomId, Pageable pageable);

    List<Chat> findByChatRoomIdAndIdGreaterThanOrderByIdAsc(Long chatRoomId, Long id, Pageable pageable);

    List<Chat> findByChatRoomIdAndIdLessThanOrderByIdDesc(Long chatRoomId, Long id, Pageable pageable);

    List<Chat> findByChatRoomIdAndIdLessThanEqualOrderByIdDesc(Long chatRoomId, Long id, Pageable pageable);

    int countByChatRoomId(Long chatRoomId);

    int countByChatRoomIdAndIdGreaterThan(Long chatRoomId, Long lastReadChatId);

    void deleteByUserIdAndChatRoomId(Long userId, Long chatRoomId);


}
