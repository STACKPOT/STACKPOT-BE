package stackpot.stackpot.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "닉네임 요청 DTO")
public class NicknameResponseDto {
    @Schema(description = "닉네임")
    private String nickname;
}
