package stackpot.stackpot.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "My Page 응답 DTO")
public class UserMyPageResponseDto {
    @Schema(description = "유저 아이디")
    private Long id;

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "역할")
    private Role role;

    @Schema(description = "유저 온도")
    private Integer userTemperature;

    @Schema(description = "유저 소개")
    private String userIntroduction;

    @Schema(description = "끓인 팟")
    private List<CompletedPotBadgeResponseDto> completedPots;

    @Schema(description = "피드")
    private List<FeedResponseDto.FeedDto> feeds;
}

