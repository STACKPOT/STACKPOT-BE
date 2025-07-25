package stackpot.stackpot.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public <T> void set(String key, T value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }


    public <T> T get(String key, Class<T> clazz) {
        Object raw = redisTemplate.opsForValue().get(key);
        if (raw == null) return null;
        return objectMapper.convertValue(raw, clazz);
    }


    public void addFeedToUserZSet(Long userId, Long feedId) {
        String key = "user:" + userId + ":feedIds";
        redisTemplate.opsForZSet().add(key, feedId.toString(), feedId);
    }

    public Set<String> getFeedIdsFromZSet(Long userId, Long cursor, int size) {
        String key = "user:" + userId + ":feedIds";
        Set<Object> rawSet;

        if (cursor == null) {
            rawSet = redisTemplate.opsForZSet().reverseRange(key, 0, size * 2L);
        } else {
            rawSet = redisTemplate.opsForZSet().reverseRangeByScore(key, 0, cursor - 1, 0, size * 2L);
        }

        if (rawSet == null || rawSet.isEmpty()) {
            return Collections.emptySet();
        }

        // Object → String 변환
        return rawSet.stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
    }


}
