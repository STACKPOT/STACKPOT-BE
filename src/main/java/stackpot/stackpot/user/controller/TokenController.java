package stackpot.stackpot.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.common.swagger.ApiErrorCodeExamples;
import stackpot.stackpot.user.dto.request.TokenRequestDto;
import stackpot.stackpot.user.dto.response.TokenServiceResponse;
import stackpot.stackpot.user.service.TokenService;

@RequiredArgsConstructor
@RestController
public class TokenController {

    private final TokenService tokenService;

    @PostMapping("/reissue")
    @Operation(summary = "토큰 재발급 API",
            description = "AccessToken 만료 시 토큰을 재발급 합니다.AccessToken, RefreshToken과 함께 요청 시 토큰을 재발급합니다. 기존의 토큰은 사용할 수 없습니다. ")
    @ApiErrorCodeExamples({
            ErrorStatus.EXPIRED_REFRESH_TOKEN,
            ErrorStatus.INVALID_AUTH_TOKEN,
            ErrorStatus.USER_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<TokenServiceResponse>> getToken(@RequestBody TokenRequestDto refreshToken) {
        return ResponseEntity.ok(ApiResponse.onSuccess(tokenService.generateAccessToken(refreshToken.getRefreshToken())));
    }
}