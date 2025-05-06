package stackpot.stackpot.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.user.dto.response.UserResponseDto;
import stackpot.stackpot.user.service.TokenService;
import stackpot.stackpot.user.dto.request.TokenRequestDto;
import stackpot.stackpot.user.dto.response.TokenServiceResponse;

@RequiredArgsConstructor
@RestController
public class TokenController {

private final TokenService tokenService;
   @PostMapping("/reissue")
    @Operation(summary = "토큰 재발급 API",
            description = "AccessToken 만료 시 토큰을 재발급 합니다.AccessToken, RefreshToken과 함께 요청 시 토큰을 재발급합니다. 기존의 토큰은 사용할 수 없습니다. ",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "성공적으로 토큰 재발급",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenServiceResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "토큰이 유효하지 않거나, 존재하지 않습니다.",
                            content = @Content(mediaType = "application/json")
                    ),
            }
    )
    public ResponseEntity<ApiResponse<TokenServiceResponse>> getToken(@RequestBody TokenRequestDto refreshToken){
        return ResponseEntity.ok(ApiResponse.onSuccess(tokenService.generateAccessToken(refreshToken.getRefreshToken())));
    }
}