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
        RefreshToken refreshToken1 = refreshTokenRepository.findById(refreshToken.getRefreshToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        User user = userRepository.findByEmail(refreshToken1.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return jwtTokenProvider.createToken(user);
    }
}