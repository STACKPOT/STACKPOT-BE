package stackpot.stackpot.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenRequestDto {

    @JsonProperty("refreshToken")
    private String refreshToken;
}
