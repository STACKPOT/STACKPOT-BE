package stackpot.stackpot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import stackpot.stackpot.config.security.JwtTokenProvider;
import stackpot.stackpot.domain.RefreshToken;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.repository.RefreshTokenRepository;
import stackpot.stackpot.repository.UserRepository.UserRepository;
import stackpot.stackpot.web.dto.TokenServiceResponse;

@RequiredArgsConstructor
@Transactional
@Service
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public TokenServiceResponse generateAccessToken(final RefreshToken refreshToken) {


        if (!jwtTokenProvider.validateToken(refreshToken.getRefreshToken())) {
            refreshTokenRepository.deleteById(refreshToken.getAccessToken()); // 유효하지 않은 토큰 삭제
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token. Please log in again.");
        }

        RefreshToken refreshToken1 = refreshTokenRepository.findById(refreshToken.getAccessToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token not found."));


        if(!refreshToken1.getRefreshToken().equals(refreshToken.getRefreshToken())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "올바르지 않은 토큰입니다.");
        }

        User user = userRepository.findByEmail(jwtTokenProvider.getEmailFromToken(refreshToken1.getRefreshToken()))
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        refreshTokenRepository.deleteById(refreshToken.getAccessToken());
        return jwtTokenProvider.createToken(user);
    }
}