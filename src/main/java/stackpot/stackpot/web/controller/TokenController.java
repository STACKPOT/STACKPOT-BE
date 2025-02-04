package stackpot.stackpot.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.domain.RefreshToken;
import stackpot.stackpot.service.TokenService;
import stackpot.stackpot.web.dto.TokenServiceResponse;

@RequiredArgsConstructor
@RestController
public class TokenController {

private final TokenService tokenService;
//    @PostMapping("/reissue")
//    @Operation(summary = "토큰 재발급 API", description = "AccessToken 만료 시 토큰을 재발급 합니다.AccessToken, RefreshToken과 함께 요청 시 토큰을 재발급합니다. 기존의 토큰은 사용할 수 없습니다. ")
//    public ResponseEntity<ApiResponse<TokenServiceResponse>> getToken(@RequestBody RefreshToken refreshToken) {
//        return ResponseEntity.ok(ApiResponse.onSuccess(tokenService.generateAccessToken(refreshToken)));
//    }
}