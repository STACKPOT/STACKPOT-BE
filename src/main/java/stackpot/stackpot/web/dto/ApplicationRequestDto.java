package stackpot.stackpot.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationRequestDto {
    @NotBlank(message = "팟 역할은 필수입니다.")
    private String potRole;

    private Boolean liked;
}