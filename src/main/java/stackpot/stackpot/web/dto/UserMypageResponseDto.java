package stackpot.stackpot.web.dto;

import lombok.*;
import stackpot.stackpot.domain.enums.Category;
import stackpot.stackpot.domain.enums.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserMypageResponseDto {
    private Long id;
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
        private Map<String, Integer> roleCounts; // 역할별 참여자 수 추가
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
        private String createdAt;
    }
}

