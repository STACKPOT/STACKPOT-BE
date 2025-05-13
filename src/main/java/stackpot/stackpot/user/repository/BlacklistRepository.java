package stackpot.stackpot.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.TokenHandler;
import stackpot.stackpot.config.security.JwtTokenProvider;

import java.util.concurrent.TimeUnit;

@Repository
@Slf4j
public class BlacklistRepository {
    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public BlacklistRepository(StringRedisTemplate redisTemplate, UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }


    // Access Token을 블랙리스트에 추가
    public void addToBlacklist(String accessToken, long expirationTime) {
        String key = "blacklist:" + accessToken;
        try{
            redisTemplate.opsForValue().set(key, "blacklisted", expirationTime, TimeUnit.MILLISECONDS);
        }
        catch (Exception e){
            log.debug("blacklist 등록 실패{}",e.getMessage());
            throw new TokenHandler(ErrorStatus.REDIS_BLACKLIST_SAVE_FAILED);
        }

    }

    // 블랙리스트 확인
    public boolean isBlacklisted(String accessToken) {
        String key = "blacklist:" + accessToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}