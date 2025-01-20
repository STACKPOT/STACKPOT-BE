
//package stackpot.stackpot.config.security;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//
//@EnableWebSecurity
//@Configuration
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests((requests) -> requests
//                        .requestMatchers("/", "/home", "/signup","/swagger-ui/**", "/v3/api-docs/**").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .formLogin((form) -> form
//                        .loginPage("/login")
//                        .defaultSuccessUrl("/home", true)
//                        .permitAll()
//                )
//                .logout((logout) -> logout
//                        .logoutUrl("/logout")
//                        .logoutSuccessUrl("/login?logout")
//                        .permitAll()
//                )
//                .oauth2Login(oauth2 -> oauth2
//                        .loginPage("/login")
//                        .permitAll()
//                );
//
//        return http.build();
//    }
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//}

//            if (userRepository.findByEmail(email).isPresent()) {
//                // 이메일이 존재하면 홈으로 리다이렉트
//                response.sendRedirect("/home");
//            } else {
//                // 이메일이 없으면 회원가입 페이지로 리다이렉트
//                response.sendRedirect("/signup");

package stackpot.stackpot.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import stackpot.stackpot.repository.UserRepository.UserRepository;
import stackpot.stackpot.web.dto.TokenServiceResponse;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOAuth2UserService oAuth2UserService;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() { // security를 적용하지 않을 리소스
        return web -> web.ignoring()
                // error endpoint를 열어줘야 함, favicon.ico 추가!
                .requestMatchers("/error", "/favicon.ico");
    }

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtTokenProvider jwtTokenProvider) throws Exception {
    http.formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable)
            .cors(withDefaults())
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
            .sessionManagement(
                    session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .oauth2Login(
                    oauth -> oauth.userInfoEndpoint(config -> config.userService(customOAuth2UserService))
                                                .successHandler(successHandler(jwtTokenProvider)))
            .authorizeHttpRequests(request -> request
                            .requestMatchers("/", "/home", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs/**").permitAll()
                            .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
//    http
//            .authorizeHttpRequests(auth -> auth
//                    .requestMatchers("/", "/home", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
//                    .requestMatchers("/login/token").authenticated()
//                    .anyRequest().permitAll()
//            )
//            .csrf(csrf -> csrf.ignoringRequestMatchers("/login/token"))
//            .oauth2Login(oauth2 -> oauth2
//                    .userInfoEndpoint().userService(customOAuth2UserService)
//                    .and()
//                    .successHandler(successHandler(jwtTokenProvider))
//            )
//            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
    @Bean
    public AuthenticationSuccessHandler successHandler(JwtTokenProvider jwtTokenProvider) {
        return (request, response, authentication) -> {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = (String) oAuth2User.getAttributes().get("email");

            // JWT 토큰 생성
            TokenServiceResponse token = jwtTokenProvider.createToken(email);
            System.out.println("STACKPOT TOKEN : " + token.getAccessToken());

            // 응답에 JWT 추가
            response.setHeader("Authorization", "Bearer " + token.getAccessToken());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            // JSON 응답 반환
            response.getWriter().write("{\"accessTokendf\": \"" + token.getAccessToken() + "\"}");
            response.getWriter().flush();
        };
    }
}