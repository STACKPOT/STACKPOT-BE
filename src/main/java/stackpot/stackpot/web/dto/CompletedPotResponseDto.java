package stackpot.stackpot.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Setter;
import stackpot.stackpot.domain.enums.PotModeOfOperation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletedPotResponseDto {
    private Long potId; // 팟 ID
    private String potName; // 팟 이름
    private LocalDate potStartDate; // 시작 날짜
    private LocalDate potEndDate; // 종료 날짜
    private String potDuration; // 팟 기간 설명
    private String potLan; // 사용 언어
    private String potContent; // 팟 설명
    private String potStatus; // 팟 상태
    private PotModeOfOperation potModeOfOperation; // 운영 방식
    private String potSummary; // 요약 설명
    private LocalDate recruitmentDeadline; // 모집 마감일
    private List<RecruitmentDetailResponseDto> recruitmentDetails; // 수정된 부분 // 모집 세부 정보
    private Map<String, Integer> roleCounts; // 역할별 인원
}

