package stackpot.stackpot.chat.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatRoomInfoBatchRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void chatRoomInfoBatchInsert(List<Long> potMemberIds, Long chatRoomId) {
        String sql = "INSERT INTO chat_room_info (image_url, last_read_chat_id, chat_room_id, pot_member_id) VALUES (?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(
                sql,
                potMemberIds,
                potMemberIds.size(),
                (PreparedStatement ps, Long potMemberId) -> {
                    ps.setNull(1, java.sql.Types.VARCHAR);
                    ps.setNull(2, java.sql.Types.VARCHAR);
                    ps.setLong(3, chatRoomId);
                    ps.setLong(4, potMemberId);
                }
        );
    }

    @Transactional
    public void lastReadChatIdBatchUpdate(List<Long> potMemberIds, Long chatRoomId, Long chatId) {
        String sql = "UPDATE chat_room_info " +
                "SET last_read_chat_id = GREATEST(COALESCE(last_read_chat_id, 0), ?) " +
                "WHERE pot_member_id = ? AND chat_room_id = ?";

        jdbcTemplate.batchUpdate(
                sql,
                potMemberIds,
                potMemberIds.size(),
                (PreparedStatement ps, Long potMemberId) -> {
                    ps.setLong(1, chatId);
                    ps.setLong(2, potMemberId);
                    ps.setLong(3, chatRoomId);
                });
    }
}
