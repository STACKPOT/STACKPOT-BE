package stackpot.stackpot.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.Authentication;
import stackpot.stackpot.user.repository.BlacklistRepository;

import java.io.IOException;
import java.util.Collections;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final BlacklistRepository blacklistRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, BlacklistRepository blacklistRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.blacklistRepository = blacklistRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);

        try {
            if (token != null) {
                log.info("accessToken {}",token);
                if (blacklistRepository.isBlacklisted(token)) {
                    log.debug("blacklistRepository에 토큰이 존재합니다.");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("로그아웃된 토큰입니다.");
                    return;
                }
                if (jwtTokenProvider.validateToken(token)) {
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("Authentication set in SecurityContext: {}",authentication.getName());
                } else {
                    log.debug("Invalid or expired token.");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Invalid or expired token.");
                    return;
                }
            } else {
                // 토큰이 없는 경우 AnonymousAuthenticationToken 설정 (비로그인 요청 정상 처리)
                log.info("❌ 토큰이 없음, 비로그인 요청 처리");
                Authentication anonymousAuth = new AnonymousAuthenticationToken(
                        "anonymousUser",
                        "anonymousUser",
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
                );
                SecurityContextHolder.getContext().setAuthentication(anonymousAuth);
                log.debug("SecurityContext에 AnonymousAuthenticationToken 저장");
            }
        } catch (Exception ex) {
            log.debug("Exception in JwtAuthenticationFilter: {}",ex.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Internal server error occurred.");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        log.debug("Authorization header is missing or does not start with 'Bearer '");
        return null;
    }
}