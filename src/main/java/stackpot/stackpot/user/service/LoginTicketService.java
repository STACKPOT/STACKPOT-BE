package stackpot.stackpot.user.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import stackpot.stackpot.apiPayload.exception.handler.UserHandler;
import stackpot.stackpot.user.dto.response.UserResponseDto;

@Service
@RequiredArgsConstructor
public class LoginTicketService {

	private static final String KEY_PREFIX = "login-ticket:";
	private static final Duration TTL = Duration.ofMinutes(2);

	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;

	public String issue(UserResponseDto.loginDto dto) {
		String ticket = UUID.randomUUID().toString();
		String key = KEY_PREFIX + ticket;

		String payload = toJson(dto);

		redisTemplate.opsForValue().set(key, payload, TTL);
		return ticket;
	}

	public UserResponseDto.loginDto consume(String ticket) {
		String key = KEY_PREFIX + ticket;

		String payload = redisTemplate.opsForValue().get(key);
		if (payload == null) {
			throw new UserHandler(ErrorStatus.LOGIN_TICKET_EXPIRED);
		}

		// 1회용 처리
		redisTemplate.delete(key);

		return fromJson(payload);
	}

	private String toJson(UserResponseDto.loginDto dto) {
		try {
			return objectMapper.writeValueAsString(dto);
		} catch (JsonProcessingException e) {
			throw new UserHandler(ErrorStatus.LOGIN_TICKET_SERIALIZE_FAILED);
		}
	}

	private UserResponseDto.loginDto fromJson(String payload) {
		try {
			return objectMapper.readValue(payload, UserResponseDto.loginDto.class);
		} catch (JsonProcessingException e) {
			throw new UserHandler(ErrorStatus.LOGIN_TICKET_DESERIALIZE_FAILED);
		}
	}
}
