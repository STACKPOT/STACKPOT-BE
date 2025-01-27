package stackpot.stackpot.converter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.Taskboard;
import stackpot.stackpot.domain.mapping.PotMember;
import stackpot.stackpot.web.dto.MyPotTaskRequestDto;
import stackpot.stackpot.web.dto.MyPotTaskResponseDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TaskboardConverterImpl implements TaskboardConverter{
    @Override
    public Taskboard toTaskboard(Pot pot, MyPotTaskRequestDto.create requset) {
        return Taskboard.builder()
                .title(requset.getTitle())
                .description(requset.getDescription())
                .endDate(requset.getDeadline())
                .startDate(requset.getDeadline())
                .status(requset.getTaskboardStatus())
                .pot(pot)
                .build();
    }
    @Override
    public MyPotTaskResponseDto toDTO(Taskboard taskboard) {
        return MyPotTaskResponseDto.builder()
                .taskboardId(taskboard.getTaskboardId())
                .title(taskboard.getTitle())
                .description(taskboard.getDescription())
                .status(taskboard.getStatus())
                .potId(taskboard.getPot().getPotId())
                .build();
    }
    @Override

    public List<MyPotTaskResponseDto.Participant> toParticipantDtoList(List<PotMember> participants) {
        return participants.stream()
                .map(this::toParticipantDto)
                .collect(Collectors.toList());
    }

    @Override
    public MyPotTaskResponseDto.Participant toParticipantDto(PotMember participant) {

        return MyPotTaskResponseDto.Participant.builder()
                .potMemberId(participant.getPotMemberId())
                .userId(participant.getUser().getUserId())
                .nickName(participant.getUser().getNickname() + " " + getVegetableNameByRole(participant.getRoleName().toString()))
                .build();
    }

    private String getVegetableNameByRole(String role) {
        Map<String, String> roleToVegetableMap = Map.of(
                "BACKEND", " 양파",
                "FRONTEND", " 버섯",
                "DESIGN", " 브로콜리",
                "PLANNING", " 당근"
        );
        return roleToVegetableMap.getOrDefault(role, "알 수 없음");
    }
}
