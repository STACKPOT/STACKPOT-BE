package stackpot.stackpot.pot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompletedPotDetailResponseDto {
    private Long potId; // 팟 ID
    private String potName; // 팟 이름
    private Long userId; // 유저 ID
    @Schema(description = "팟 시작 날짜 (형식: yyyy. MM. dd)", example = "2025.02.18")
    private String potStartDate; // 시작 날짜
    @Schema(description = "팟 종료 날짜 (형식: yyyy. MM. dd)", example = "2025.03.05")
    private String potEndDate; // 종료 날짜
    private String potContent; // 팟 설명
    private String potStatus; // 팟 상태
    private String potSummary; // 요약 설명
    private String appealContent;
    private String userPotRole;
}

