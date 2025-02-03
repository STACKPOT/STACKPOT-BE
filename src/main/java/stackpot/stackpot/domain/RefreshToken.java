package stackpot.stackpot.domain;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@RedisHash(value = "refreshToken", timeToLive = 14440)
public class RefreshToken {

    @Id
    private String refreshToken;
    private String accessToken;

    public RefreshToken(String refreshToken, String accessToken){
        this.refreshToken = refreshToken;
        this.accessToken =accessToken;
    }

}
