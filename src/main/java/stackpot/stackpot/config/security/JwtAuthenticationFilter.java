package stackpot.stackpot.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.Authentication;
import stackpot.stackpot.repository.BlacklistRepository;
import stackpot.stackpot.repository.RefreshTokenRepository;
import stackpot.stackpot.repository.UserRepository.UserRepository;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

private final BlacklistRepository blacklistRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, BlacklistRepository blacklistRepository, RefreshTokenRepository refreshTokenRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.blacklistRepository = blacklistRepository;

        this.refreshTokenRepository = refreshTokenRepository;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);

        try {
            if (token != null) {
                System.out.println("Token found: " + token);
                if (blacklistRepository.isBlacklisted(token)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("로그아웃된 토큰입니다.");
                    return;
                }
                if (jwtTokenProvider.validateToken(token)) {
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("Authentication set in SecurityContext: " + authentication.getName());
                } else {
                    //  액세스 토큰이 만료됨 → 리프레시 토큰 확인
                    System.out.println("Access token expired, checking refresh token...");
                    String refreshToken = request.getHeader("Refresh-Token");

                    if (refreshToken != null) {
                        if (!refreshTokenRepository.validateRefreshToken(refreshToken)) {
                            //  리프레시 토큰도 만료됨 → 401 반환
                            refreshTokenRepository.deleteToken(refreshToken);
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("Refresh token expired. 다시 로그인하세요.");
                            return;
                        }

                        //  리프레시 토큰이 유효하면 새 액세스 토큰 발급
                        String newAccessToken = jwtTokenProvider.refreshAccessToken(refreshToken);
                        if (newAccessToken == null) {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("Refresh token expired. 다시 로그인하세요.");
                            return;
                        }

                        //  새 액세스 토큰을 응답 헤더에 추가
                        response.setHeader("Authorization", "Bearer " + newAccessToken);
                    } else {
                        //  리프레시 토큰도 없으면 401 반환
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("Invalid or expired token.");
                        return;
                    }
                }
            } else {
                // 토큰이 없는 경우 AnonymousAuthenticationToken 설정 (비로그인 요청 정상 처리)
                System.out.println("🔹 토큰이 없음, 비로그인 요청 처리");
                Authentication anonymousAuth = new AnonymousAuthenticationToken(
                        "anonymousUser",
                        "anonymousUser",
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
                );
                SecurityContextHolder.getContext().setAuthentication(anonymousAuth);
//                System.out.println("✅ SecurityContext에 AnonymousAuthenticationToken 저장됨");
            }
        } catch (Exception ex) {
            System.out.println("Exception in JwtAuthenticationFilter: " + ex.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Internal server error occurred.");
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        System.out.println("Authorization header is missing or does not start with 'Bearer '.");
        return null;
    }

}