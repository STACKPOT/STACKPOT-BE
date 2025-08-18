package stackpot.stackpot.pot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import stackpot.stackpot.user.entity.enums.Role;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class PotRequestDto {

    @NotBlank(message = "팟 이름은 필수입니다.")
    @Schema(description = "팟 이름", example = "스터디용 백엔드 프로젝트")
    private String potName;

    @Schema(description = "팟 시작 날짜 (yyyy.MM)", example = "2025.08")
    private String potStartDate;

    @Schema(description = "팟 종료 날짜 (yyyy.MM)", example = "2025.10")
    private String potEndDate;

    @Schema(description = "모집 마감일 (yyyy-MM-dd)", example = "2025-08-18")
    private LocalDate potRecruitmentDeadline;

    @Schema(description = "팟 역할", example = "BACKEND")
    private Role potRole;

    @NotBlank(message = "사용 언어는 필수입니다.")
    @Schema(description = "사용 언어", example = "Java")
    private String potLan;

    @Schema(description = "팟 설명", example = "이 프로젝트는 REST API 서버를 구현하는 것을 목표로 합니다.")
    private String potContent;

    @Schema(description = "운영 방식", example = "ONLINE")
    private String potModeOfOperation;

    @Schema(description = "팟 요약", example = "백엔드 중심의 API 개발 스터디 프로젝트")
    private String potSummary;

    @Schema(description = "모집 역할 리스트")
    private List<PotRecruitmentRequestDto> recruitmentDetails;
}
