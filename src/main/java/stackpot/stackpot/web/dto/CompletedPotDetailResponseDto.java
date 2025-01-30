package stackpot.stackpot.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompletedPotDetailResponseDto {
    private Long potId; // 팟 ID
    private String potName; // 팟 이름

    @Schema(description = "팟 시작 날짜 (형식: yyyy. M. d)", example = "2025. 2. 18")
    private String potStartDate; // 시작 날짜
    @Schema(description = "팟 종료 날짜 (형식: yyyy. M. d)", example = "2025. 3. 5")
    private String potEndDate; // 종료 날짜
    private String potContent; // 팟 설명
    private String potStatus; // 팟 상태
    private String potSummary; // 요약 설명
    private String appealContent;
}

