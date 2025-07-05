package stackpot.stackpot.config;


import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import stackpot.stackpot.apiPayload.code.ErrorResponseDto;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.common.swagger.ApiErrorCodeExample;
import stackpot.stackpot.common.swagger.ApiErrorCodeExamples;
import stackpot.stackpot.common.swagger.ExampleHolder;
import io.swagger.v3.oas.models.Operation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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


        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        OpenAPI openAPI = new OpenAPI()
                .info(info)
                .addServersItem(new Server().url("http://localhost:8080").description("Local server"))// 서버 URL 설정
                .addServersItem(new Server().url("http://dev.stackpot.co.kr").description("Dev server"))
                .addServersItem(new Server().url("https://api.stackpot.co.kr").description("Production server"))
                .addSecurityItem(securityRequirement)
                .components(components);

        openAPI.addExtension("x-swagger-ui-disable-cache", true);
        return openAPI;
    }


    @Bean
    public OperationCustomizer customize() {
        return (operation, handlerMethod) -> {
            ApiErrorCodeExamples errorCodeExamples = handlerMethod.getMethodAnnotation(ApiErrorCodeExamples.class);
            if (errorCodeExamples != null) {
                generateErrorCodeResponseExample(operation, errorCodeExamples.value());
            }

            ApiErrorCodeExample errorCodeExample = handlerMethod.getMethodAnnotation(ApiErrorCodeExample.class);
            if (errorCodeExample != null) {
                generateErrorCodeResponseExample(operation, errorCodeExample.value());
            }

            return operation;
        };
    }

    private void generateErrorCodeResponseExample(Operation operation, ErrorStatus[] errorStatuses) {
        ApiResponses responses = operation.getResponses();

        Map<Integer, List<ExampleHolder>> statusWithExampleHolders = Arrays.stream(errorStatuses)
                .map(errorStatus -> ExampleHolder.builder()
                        .holder(getSwaggerExample(errorStatus))
                        .code(errorStatus.getHttpStatus().value())
                        .name(errorStatus.name())
                        .build())
                .collect(Collectors.groupingBy(ExampleHolder::getCode));

        addExamplesToResponses(responses, statusWithExampleHolders);
    }

    private void generateErrorCodeResponseExample(Operation operation, ErrorStatus errorStatus) {
        ApiResponses responses = operation.getResponses();
        ExampleHolder exampleHolder = ExampleHolder.builder()
                .holder(getSwaggerExample(errorStatus))
                .name(errorStatus.name())
                .code(errorStatus.getHttpStatus().value())
                .build();
        addExamplesToResponses(responses, exampleHolder);
    }

    private Example getSwaggerExample(ErrorStatus errorStatus) {

        ErrorResponseDto errorResponseDto = ErrorResponseDto.of(errorStatus);

        Example example = new Example();
        example.setValue(errorResponseDto);
        return example;
    }

    private void addExamplesToResponses(ApiResponses responses,
                                        Map<Integer, List<ExampleHolder>> statusWithExampleHolders) {
        statusWithExampleHolders.forEach((status, holders) -> {
            Content content = new Content();
            MediaType mediaType = new MediaType();
            ApiResponse apiResponse = new ApiResponse();

            holders.forEach(holder -> mediaType.addExamples(holder.getName(), holder.getHolder()));
            content.addMediaType("application/json", mediaType);
            apiResponse.setContent(content);
            responses.addApiResponse(String.valueOf(status), apiResponse);
        });
    }

    private void addExamplesToResponses(ApiResponses responses, ExampleHolder exampleHolder) {
        Content content = new Content();
        MediaType mediaType = new MediaType();
        ApiResponse apiResponse = new ApiResponse();

        mediaType.addExamples(exampleHolder.getName(), exampleHolder.getHolder());
        content.addMediaType("application/json", mediaType);
        apiResponse.setContent(content);
        responses.addApiResponse(String.valueOf(exampleHolder.getCode()), apiResponse);
    }


}
