package stackpot.stackpot.web.dto;

import jakarta.validation.constraints.Pattern;
import lombok.*;
import stackpot.stackpot.Validation.annotation.ValidRole;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PotAllMemRequestDto {
    private Long potMemberId; // 팟 멤버 ID
    private Long potId; // 팟 ID
    private Long userId; // 유저 ID
    @ValidRole
    private String roleName; // 역할 이름
    private String nickname; // 닉네임 + 역할
    private Boolean isOwner; // 팟 생성자인지 여부
    private String appealContent; // 어필 내용
}

