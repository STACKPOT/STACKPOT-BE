package stackpot.stackpot.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import stackpot.stackpot.common.BaseEntity;
import stackpot.stackpot.pot.entity.mapping.PotMember;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatRoomInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_info_id")
    private Long id;

    private String imageUrl; // 채팅방 썸네일
    private Long lastReadChatId; // 마지막으로 읽은 채팅 id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pot_member_id")
    private PotMember potMember;

    public void updateLastReadChatId(Long lastReadChatId) {
        this.lastReadChatId = lastReadChatId;
    }

    public void updateThumbnail(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

