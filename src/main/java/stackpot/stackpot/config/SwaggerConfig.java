package stackpot.stackpot.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI stackPotAPI() {
        Info info = new Info()
                .title("StackPot API")
                .description("StackPotAPI API 명세서")
                .version("1.0.0");

        String jwtSchemeName = "JWT TOKEN";

        // API 요청 헤더에 인증 정보 포함
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        // SecuritySchemes 등록
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP) // HTTP 방식
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        return new OpenAPI()
                .info(info) // API 정보 설정
                .addServersItem(new Server().url("http://localhost:8080").description("Dev server")) // 서버 URL 설정
                .addServersItem(new Server().url("https://api.stackpot.co.kr").description("Production server")) // 서버 URL 설정
                .addSecurityItem(securityRequirement) // SecurityRequirement 추가
                .components(components); // SecuritySchemes 등록
    }
}
