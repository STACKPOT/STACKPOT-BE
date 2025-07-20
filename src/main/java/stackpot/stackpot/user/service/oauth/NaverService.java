package stackpot.stackpot.user.service.oauth;

import io.netty.handler.codec.http.HttpHeaderValues;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import stackpot.stackpot.user.dto.response.NaverTokenResponseDto;
import stackpot.stackpot.user.dto.response.NaverUserInfoResponseDto;

@Slf4j
@RequiredArgsConstructor
@Service
public class NaverService {

    @Value("${spring.naver.client-id}")
    private String clientId;
    @Value("${spring.naver.client-secret}")
    private String clientSecret;

    private final String TokenUrl = "https://nid.naver.com/oauth2.0/token";
    private final String UserInfoUrl = "https://openapi.naver.com/v1/nid/me";

    public String getAccessTokenFromNaver(String code) {
        NaverTokenResponseDto naverTokenResponseDto = WebClient.create(TokenUrl).post()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", clientId)
                        .queryParam("client_secret", clientSecret)
                        .queryParam("code", code)
                        .queryParam("state", "sampleState")
                        .build(true))
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(NaverTokenResponseDto.class)
                .block();

        log.info("[Naver Service] Access Token ------> {}", naverTokenResponseDto.getAccessToken());
        log.info("[Naver Service] Refresh Token ------> {}", naverTokenResponseDto.getRefreshToken());

        return naverTokenResponseDto.getAccessToken();
    }

    public NaverUserInfoResponseDto getUserInfo(String accessToken) {
        NaverUserInfoResponseDto userInfo = WebClient.create(UserInfoUrl)
                .get()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(NaverUserInfoResponseDto.class)
                .block();

        log.info("[Naver Service] Raw User Info Response: {}", userInfo);

        if (userInfo == null || userInfo.getResponse() == null) {
            log.error("[Naver Service] Invalid user info response from Naver API");
            throw new RuntimeException("Invalid user info response from Naver API");
        }

        log.info("[Naver Service] Auth ID ------> {}", userInfo.getResponse().id());
        log.info("[Naver Service] email ------> {}", userInfo.getResponse().email());

        return userInfo;
    }
}
