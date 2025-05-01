package stackpot.stackpot.user.repository;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.config.security.JwtTokenProvider;

import java.util.concurrent.TimeUnit;

@Repository
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
//    public void addToBlacklist(Long userId, String accessToken, long expirationTime) {
//        String key = "accessToken:" + userId;
//        redisTemplate.opsForValue().set(key, accessToken, expirationTime, TimeUnit.MILLISECONDS);
//    }
//
//    // 블랙리스트 확인
//    public boolean isBlacklisted(String token) {
//        String email = jwtTokenProvider.getEmailFromToken(token);
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
//        String key = "accessToken:" + user.getId();
//        return redisTemplate.hasKey(key);
//    }

    public void addToBlacklist(String accessToken, long expirationTime) {
        String key = "blacklist:" + accessToken;
        redisTemplate.opsForValue().set(key, "blacklisted", expirationTime, TimeUnit.MILLISECONDS);
    }

    // 블랙리스트 확인
    public boolean isBlacklisted(String accessToken) {
        String key = "blacklist:" + accessToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}