package stackpot.stackpot.pot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import stackpot.stackpot.Validation.annotation.ValidRole;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PotApplicationRequestDto {
    @NotBlank(message = "팟 역할은 필수입니다.")
    @ValidRole
    private String potRole;


}