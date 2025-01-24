package stackpot.stackpot.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PotMemberRequestDto {

    private List<Long> applicantIds; // 지원자 ID 리스트
}
