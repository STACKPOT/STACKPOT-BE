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
import stackpot.stackpot.user.dto.response.GoogleTokenResponseDto;
import stackpot.stackpot.user.dto.response.GoogleUserInfoResponseDto;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@Service
public class GoogleService {

    @Value("${spring.google.client-id}")
    private String clientId;
    @Value("${spring.google.client-secret}")
    private String clientSecret;

    private final String TokenUrl = "https://oauth2.googleapis.com/token";
    private final String UserInfoUrl = "https://www.googleapis.com/userinfo/v2/me";

    public String getAccessTokenFromGoogle(String code, String redirectUri) {
        GoogleTokenResponseDto googleTokenResponseDto = WebClient.create(TokenUrl).post()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", clientId)
                        .queryParam("client_secret", clientSecret)
                        .queryParam("redirect_uri", redirectUri)
                        .queryParam("code", URLDecoder.decode(code, StandardCharsets.UTF_8))
                        .build(true))
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(GoogleTokenResponseDto.class)
                .block();

        log.info("[Google Service] Access Token ------> {}", googleTokenResponseDto.getAccessToken());

        return googleTokenResponseDto.getAccessToken();
    }

    public GoogleUserInfoResponseDto getUserInfo(String accessToken) {
        GoogleUserInfoResponseDto userInfo = WebClient.create(UserInfoUrl)
                .get()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(GoogleUserInfoResponseDto.class)
                .block();

        log.info("[Google Service] Raw User Info Response: {}", userInfo);

        if (userInfo == null) {
            log.error("[Google Service] Invalid user info response from Google API");
            throw new RuntimeException("Invalid user info response from Google API");
        }

        log.info("[Google Service] Auth ID ------> {}", userInfo.getId());
        log.info("[Google Service] email ------> {}", userInfo.getEmail());

        return userInfo;
    }
}
