package stackpot.stackpot.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.web.dto.OngoingPotResponseDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class MyPotConverterImpl implements MyPotConverter{

    public OngoingPotResponseDto convertToOngoingPotResponseDto(Pot pot) {
        // Role별 인원 수 집계
        Map<String, Integer> roleCountMap = pot.getPotMembers().stream()
                .collect(Collectors.groupingBy(
                        member -> member.getRoleName().name(), // Role Enum을 문자열로 변환
                        Collectors.reducing(0, e -> 1, Integer::sum) // 각 역할의 개수를 세기
                ));

        return OngoingPotResponseDto.builder()
                .potId(pot.getPotId())
                .potName(pot.getPotName())
                .potStatus(pot.getPotStatus())  // 진행 중인 팟 상태
                .members(roleCountMap)  // 역할 개수 Map 적용
                .build();
    }
}
