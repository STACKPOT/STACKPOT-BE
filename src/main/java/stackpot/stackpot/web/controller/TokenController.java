package stackpot.stackpot.web.controller;

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
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenServiceResponse>> getToken(@RequestBody RefreshToken refreshToken) {
        return ResponseEntity.ok(ApiResponse.onSuccess(tokenService.generateAccessToken(refreshToken)));
    }
}