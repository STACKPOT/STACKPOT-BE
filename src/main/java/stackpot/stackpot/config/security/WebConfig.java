package stackpot.stackpot.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // 모든 엔드포인트에 대해 CORS 허용
                        .allowedOrigins("http://localhost:3000", "https://stackpot.co.kr") // 허용할 도메인
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
                        .allowedHeaders("Authorization", "Content-Type", "X-Requested-With") // 허용할 헤더
                        .allowCredentials(true) // 인증 정보 포함 (쿠키, 헤더 등)
                        .maxAge(3600); // CORS 요청의 캐싱 시간 (1시간)
            }
        };
    }
}