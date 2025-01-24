package stackpot.stackpot.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAppealRequestDto {

    private String appealContent;
}

