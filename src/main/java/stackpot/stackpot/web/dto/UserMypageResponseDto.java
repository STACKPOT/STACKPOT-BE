package stackpot.stackpot.web.dto;

import lombok.*;
import stackpot.stackpot.domain.enums.Category;
import stackpot.stackpot.domain.enums.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserMypageResponseDto {
    private String email;
    private String nickname;
    private Role role;
    private String interest;
    private Integer userTemperature;
    private String kakaoId;
    private String userIntroduction;
    private List<CompletedPotDto> completedPots;
    private List<FeedDto> feeds;

    @Getter
    @Setter
    @Builder
    public static class CompletedPotDto {
        private Long potId;
        private String potName;
        private String potStartDate;
        private String potEndDate;
        private String potSummary;
        private List<PotRecruitmentResponseDto> recruitmentDetails;
    }

    @Getter
    @Setter
    @Builder
    public static class FeedDto {
        private Long feedId;
        private String title;
        private String content;
        private Category category;
        private Long likeCount;
        private LocalDateTime createdAt;
    }
}

