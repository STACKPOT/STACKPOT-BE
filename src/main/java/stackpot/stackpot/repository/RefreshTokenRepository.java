package stackpot.stackpot.repository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class RefreshTokenRepository {

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public RefreshTokenRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

//    // Refresh Token 저장 (TTL 포함)
//    public void saveToken(Long userId, String refreshToken, long expirationTime) {
//        String key = "refreshToken:" + userId;
//        redisTemplate.opsForValue().set(key, refreshToken, expirationTime, TimeUnit.MILLISECONDS);
//    }
//
//    // Refresh Token 가져오기
//    public String getRefreshToken(Long userId) {
//        return redisTemplate.opsForValue().get("refreshToken:" + userId);
//    }
//
//    // Refresh Token 삭제
//    public void deleteRefreshToken(Long userId) {
//        redisTemplate.delete("refreshToken:" + userId);
//    }

    public void saveToken(Long userId, String refreshToken, long expirationTime) {
        String key = "refreshToken:" + refreshToken; //
        redisTemplate.opsForValue().set(key, userId.toString(), expirationTime / 1000, TimeUnit.SECONDS);
    }

    // Refresh Token으로 userId 가져오기
    public Long getUserIdByToken(String refreshToken) {
        String key = "refreshToken:" + refreshToken;
        String userIdStr = redisTemplate.opsForValue().get(key);
        return userIdStr != null ? Long.parseLong(userIdStr) : null;
    }

    // Refresh Token 삭제
    public void deleteToken(String refreshToken) {
        String key = "refreshToken:" + refreshToken;
        redisTemplate.delete(key);
    }

    public boolean existsByToken(String refreshToken) {
        String key = "refreshToken:" + refreshToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}