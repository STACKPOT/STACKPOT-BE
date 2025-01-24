package stackpot.stackpot.web.dto;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.mapping.PotApplication;

import java.time.LocalDate;

@Getter
@Builder
public class PotMemberResponseDTO {

        private Long potMemberId;
        private String roleName;
        private Boolean owner;
        private String appealContent;
}
