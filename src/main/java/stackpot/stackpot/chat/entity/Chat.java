package stackpot.stackpot.chat.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import stackpot.stackpot.common.BaseEntity;

@Document(collection = "chat")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chat extends BaseEntity {

    @Id
    private String id;

    private String message;
    private String fileUrl;
    private Long userId;
    private String userName;
    private Long chatRoomId;
}
