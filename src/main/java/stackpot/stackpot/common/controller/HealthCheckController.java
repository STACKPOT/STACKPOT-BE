package stackpot.stackpot.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/health")
@Tag(name = "HealthCheck API", description = "HealthCheck API")
public class HealthCheckController {

    @GetMapping
    @Operation(
            summary = "Back-End Server HealthCheck API",
            description = "서버 헬스 체크 API 입니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(mediaType = "application/json", schema = @Schema(type = "string"))
                    )
            }
    )
    public ResponseEntity<String> healthCheck() {
        log.info("Back-End Server Health");
        return ResponseEntity.ok("ok");
    }
}
