package stackpot.stackpot.chat.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatRoomInfoBatchRepository {

    private final JdbcTemplate jdbcTemplate;

    public void lastReadChatIdBatchUpdate(List<Long> potMemberIds, Long chatRoomId, String chatId) {
        String sql = "UPDATE chat_room_info SET last_read_chat_id = GREATEST(last_read_chat_id, ?) WHERE pot_member_id = ? AND chat_room_id = ?";
        jdbcTemplate.batchUpdate(
                sql,
                potMemberIds,
                potMemberIds.size(),
                (PreparedStatement ps, Long potMemberId) -> {
                    ps.setString(1, chatId);
                    ps.setLong(2, potMemberId); // WHERE 절
                    ps.setLong(3, chatRoomId); // WHERE 절
                });
    }
}
