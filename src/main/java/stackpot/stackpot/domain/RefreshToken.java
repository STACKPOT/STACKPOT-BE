package stackpot.stackpot.domain;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.Stack;

@Getter
@RedisHash(value = "accessToken", timeToLive = 86400)
public class RefreshToken {

    @Id
    private String accessToken;
    private String refreshToken;

    public RefreshToken(String accessToken, String refreshToken){
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

}
