package stackpot.stackpot.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import stackpot.stackpot.apiPayload.code.BaseErrorCode;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.TokenHandler;
import stackpot.stackpot.apiPayload.exception.handler.UserHandler;
import stackpot.stackpot.config.security.JwtTokenProvider;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.repository.RefreshTokenRepository;
import stackpot.stackpot.user.repository.UserRepository;
import stackpot.stackpot.user.dto.response.TokenServiceResponse;

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
            throw new TokenHandler(ErrorStatus.EXPIRED_REFRESH_TOKEN);
        }

        if(!refreshTokenRepository.existsByToken(refreshToken)){
            throw new TokenHandler(ErrorStatus.INVALID_AUTH_TOKEN);
        }

        Long userId = refreshTokenRepository.getUserIdByToken(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        refreshTokenRepository.deleteToken(refreshToken);
        return jwtTokenProvider.createToken(user.getUserId(), user.getProvider(), user.getUserType(), user.getEmail());
    }

}