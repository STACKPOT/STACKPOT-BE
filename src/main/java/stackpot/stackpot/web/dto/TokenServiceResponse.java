package stackpot.stackpot.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Id;
import lombok.Getter;
import stackpot.stackpot.domain.enums.Role;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenServiceResponse {


    private final Role role;
    private final String accessToken;
    private final String refreshToken;

    public TokenServiceResponse(String accessToken, String refreshToken) {
        this.role = null;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public TokenServiceResponse(Role role, String accessToken, String refreshToken) {
        this.role = role;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static TokenServiceResponse of(final Role role, final String accessToken, final String refreshToken) {
        return new TokenServiceResponse(role, accessToken, refreshToken);
    }

    public static TokenServiceResponse withoutRole(final String accessToken, final String refreshToken) {
        return new TokenServiceResponse(accessToken, refreshToken);
    }
//    public TokenServiceResponse withoutRefreshToken() {
//        return new TokenServiceResponse(this.accessToken, this.refreshToken);
//    }
}