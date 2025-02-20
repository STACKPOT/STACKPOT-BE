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

    public boolean validateRefreshToken(String refreshToken) {
        // Redis에서 해당 리프레시 토큰이 존재하는지 확인
        if (!existsByToken(refreshToken)) {
            return false; // Redis에 없으면 이미 만료되었거나 삭제된 토큰
        }

        // userId를 조회하여 검증 (만료 여부 확인)
        Long userId = getUserIdByToken(refreshToken);
        return userId != null;
    }
}