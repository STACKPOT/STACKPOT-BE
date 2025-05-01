package stackpot.stackpot.user.dto;

import lombok.*;
import stackpot.stackpot.badge.dto.CompletedPotBadgeResponseDto;
import stackpot.stackpot.user.entity.enums.Role;
import stackpot.stackpot.feed.dto.FeedResponseDto;

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

