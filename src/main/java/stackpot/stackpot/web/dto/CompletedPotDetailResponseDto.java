package stackpot.stackpot.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompletedPotDetailResponseDto {
    private Long potId; // 팟 ID
    private String potName; // 팟 이름
    private String potStartDate; // 시작 날짜
    private String potEndDate; // 종료 날짜
    private String potContent; // 팟 설명
    private String potStatus; // 팟 상태
    private String potSummary; // 요약 설명
    private String appealContent;
}

