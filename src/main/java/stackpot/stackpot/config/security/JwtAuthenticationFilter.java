package stackpot.stackpot.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.Authentication;
import stackpot.stackpot.repository.BlacklistRepository;

import java.io.IOException;
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
            this.jwtTokenProvider = jwtTokenProvider;
    }

//    private final BlacklistRepository blacklistRepository;

//    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, BlacklistRepository blacklistRepository) {
//        this.jwtTokenProvider = jwtTokenProvider;
//        this.blacklistRepository = blacklistRepository;
//
//    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);

        try {
            if (token != null) {
                System.out.println("Token found: " + token);
//                if (blacklistRepository.isBlacklisted(token)) {
//                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                    response.getWriter().write("로그아웃된 토큰입니다.");
//                    return;
//                }
                if (jwtTokenProvider.validateToken(token)) {
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("Authentication set in SecurityContext: " + authentication.getName());
                } else {
                    System.out.println("Invalid or expired token.");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Invalid or expired token.");
                    return;
                }
            } else {
                System.out.println("No token found in the request.");
            }
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            System.out.println("Exception in JwtAuthenticationFilter: " + ex.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Internal server error occurred.");
        }
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