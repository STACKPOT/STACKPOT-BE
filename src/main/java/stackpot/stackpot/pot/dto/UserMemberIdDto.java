package stackpot.stackpot.pot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserMemberIdDto {

    private Long potMemberId;
    private Long potId;
}
