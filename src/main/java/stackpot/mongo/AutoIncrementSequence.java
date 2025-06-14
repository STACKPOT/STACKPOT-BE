package stackpot.mongo;

import jakarta.persistence.Id;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "auto_sequence")
public class AutoIncrementSequence {

    @Id
    private String id;

    private Long sequence;
}
