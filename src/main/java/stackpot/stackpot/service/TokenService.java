package stackpot.stackpot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import stackpot.stackpot.config.security.JwtTokenProvider;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.repository.RefreshTokenRepository;
import stackpot.stackpot.repository.UserRepository.UserRepository;
import stackpot.stackpot.web.dto.TokenRequestDto;
import stackpot.stackpot.web.dto.TokenServiceResponse;

@RequiredArgsConstructor
@Transactional
@Service
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public TokenServiceResponse generateAccessToken(String refreshToken) {

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            refreshTokenRepository.deleteToken(refreshToken);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token. Please log in again.");
        }

        if(!refreshTokenRepository.existsByToken(refreshToken)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "올바르지 않은 토큰입니다.");
        }

        Long userId = refreshTokenRepository.getUserIdByToken(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        refreshTokenRepository.deleteToken(refreshToken);
        return jwtTokenProvider.createToken(user);
    }

}