

package stackpot.stackpot.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import stackpot.stackpot.repository.UserRepository.UserRepository;

@EnableWebSecurity
@Configuration

public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtTokenProvider jwtTokenProvider, UserRepository userRepository) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/home",
                                "/pots/**",
                                "/signup",
                                "/user/profile",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/signup")) // CSRF 예외 처리
                .cors() // CORS 활성화
                .and()
//                .formLogin(form -> form
//                        .loginPage("/login").permitAll()
//                )
//                .oauth2Login(oauth2 -> oauth2
//                        .loginPage("/login")
//                        .successHandler(successHandler(jwtTokenProvider, userRepository))
//                )
                // JWT 필터 추가
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        return (request, response, authentication) -> {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = (String) oAuth2User.getAttributes().get("email");

            // JWT 토큰 생성
            String token = jwtTokenProvider.createToken(email);

            response.setHeader("Authorization", "Bearer " + token);

            if (userRepository.findByEmail(email).isPresent()) {
                // 이메일이 존재하면 홈으로 리다이렉트
                response.sendRedirect("/home");
            } else {
                // 이메일이 없으면 회원가입 페이지로 리다이렉트
                response.sendRedirect("/signup");
            }
        };
    }


}
