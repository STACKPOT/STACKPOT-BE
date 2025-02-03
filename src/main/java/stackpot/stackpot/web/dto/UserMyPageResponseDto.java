package stackpot.stackpot.web.dto;

import lombok.*;
import stackpot.stackpot.domain.enums.Role;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserMyPageResponseDto {
    private Long id;
    private String nickname;
    private Role role;
    private Integer userTemperature;
    private String userIntroduction;
    private List<CompletedPotBadgeResponseDto> completedPots;
    private List<FeedResponseDto.FeedDto> feeds;
}

