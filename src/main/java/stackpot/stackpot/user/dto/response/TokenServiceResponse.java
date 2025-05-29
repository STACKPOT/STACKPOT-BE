package stackpot.stackpot.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenServiceResponse {

    private final String accessToken;
    private final String refreshToken;

    public TokenServiceResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static TokenServiceResponse of(final String accessToken, final String refreshToken) {
        return new TokenServiceResponse(accessToken, refreshToken);
    }
}