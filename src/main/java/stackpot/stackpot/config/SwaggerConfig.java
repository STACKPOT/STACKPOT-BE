package stackpot.stackpot.config;

import com.amazonaws.services.cloudformation.model.Tag;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI StackPotAPI() {
        Info info = new Info()
                .title("StackPot API")
                .description("StackPot API 명세서")
                .version("1.0.0");

        String jwtSchemeName = "JWT TOKEN";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER));


        // 서버 URL 설정
        return new OpenAPI()
                .addServersItem(new Server().url("http://localhost:8080").description("Local server")) // 로컬 서버
                .addServersItem(new Server().url("https://api.stackpot.com").description("Production server")) // 프로덕션 서버
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}
