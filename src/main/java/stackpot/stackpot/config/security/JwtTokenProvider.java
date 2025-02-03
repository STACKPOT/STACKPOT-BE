package stackpot.stackpot.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import stackpot.stackpot.domain.RefreshToken;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.repository.RefreshTokenRepository;
import stackpot.stackpot.web.dto.TokenServiceResponse;

import java.security.Key;
import java.util.Date;

import static org.hibernate.query.sqm.tree.SqmNode.log;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtTokenProvider {

    private final RefreshTokenRepository refreshTokenRepository;
    
    @Value("${jwt.secret}")
    private String secretKey;
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60; 	//1시간
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24; // 1일
    private final UserDetailsService  userDetailsService;

    // JWT 생성 (이메일 포함)
    public TokenServiceResponse createToken(User user) {
        Claims claims = Jwts.claims().setSubject(user.getEmail());

        Date now = new Date();

        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        RefreshToken redis = new RefreshToken(refreshToken, user.getEmail());
        refreshTokenRepository.save(redis);

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

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            return expiration.before(new Date()); // 현재 시간보다 이전이면 만료됨
        } catch (ExpiredJwtException e) {
            return true; // 이미 만료된 토큰
        }
    }

    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Authentication getAuthentication(String token) {
        String email = getEmailFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email); // 이메일로 사용자 로드
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
}