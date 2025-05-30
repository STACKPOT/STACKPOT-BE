package stackpot.stackpot.config.security;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.TokenHandler;
import stackpot.stackpot.user.dto.response.TokenServiceResponse;
import stackpot.stackpot.user.entity.TempUser;
import stackpot.stackpot.user.entity.enums.Provider;
import stackpot.stackpot.user.entity.enums.UserType;
import stackpot.stackpot.user.repository.RefreshTokenRepository;
import stackpot.stackpot.user.repository.TempUserRepository;

import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtTokenProvider {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TempUserRepository tempUserRepository;

    @Value("${spring.jwt.secret}")
    private String secretKey;

    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24; //1일
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 14; // 14일
    private static final long TEMP_TOKEN_EXPIRE_TIME = 1000 * 60 * 20;  // 20분

    private final UserDetailsService userDetailsService;

    // JWT 생성
    public TokenServiceResponse createToken(Long userId, Provider provider, UserType userType, String email) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
        claims.put("provider", provider);
        claims.put("userType", userType);
        claims.put("email", email);

        Date now = new Date();

        if (userType == UserType.TEMP) {
            String accessToken = Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(now)
                    .setExpiration(new Date(now.getTime() + TEMP_TOKEN_EXPIRE_TIME))
                    .signWith(SignatureAlgorithm.HS256, secretKey)
                    .compact();
            return TokenServiceResponse.of(accessToken, null);
        }
        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        String refreshToken = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        long expiration = getExpiration(refreshToken);
        refreshTokenRepository.saveToken(userId, refreshToken, expiration);

        return TokenServiceResponse.of(accessToken, refreshToken);
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            log.warn("JWT Token expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT Token: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT Token: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String userType = (String) claims.get("userType"); // "TEMP" | "USER" | "ADMIN"
        Long tempUserId = Long.valueOf(claims.getSubject());

        // TEMP 사용자
        if ("TEMP".equals(userType)) {
            TempUser tempUser = tempUserRepository.findById(tempUserId)
                    .orElseThrow(() -> new RuntimeException("TempUser not found"));

            List<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_TEMP")
            );
            return new UsernamePasswordAuthenticationToken(tempUser, null, authorities);
        }

        // USER, ADMIN 사용자
        UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(tempUserId));
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + userType)
        );
        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }

    public long getExpiration(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().getTime() - System.currentTimeMillis();
        } catch (ExpiredJwtException e) {
            return 0; // 이미 만료된 경우 0 반환
        } catch (Exception e) {
            throw new TokenHandler(ErrorStatus.INVALID_AUTH_TOKEN);
        }
    }

    public Long extractUserIdFromJwt(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }
}