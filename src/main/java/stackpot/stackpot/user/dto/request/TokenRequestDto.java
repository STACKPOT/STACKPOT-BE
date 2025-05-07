package stackpot.stackpot.user.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Token 갱신 요청 DTO")
public class TokenRequestDto {

    @JsonProperty("refreshToken")
    @Schema(description = "refresh token", example="qwer.asdf.zxcv")
    private String refreshToken;
}
