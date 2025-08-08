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

    @Schema(description = "시리즈 이름")
    private List<String> seriesComments;

    @Schema(description = "피드")
    private List<FeedResponseDto.FeedDto> feeds;
}

