package stackpot.stackpot.web.dto;

import lombok.Builder;
import lombok.Getter;
import stackpot.stackpot.domain.enums.Role;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class PotMemberSummaryResponseDTO {
    private Map<String, Long> roleCounts; // 역할별 인원수
}