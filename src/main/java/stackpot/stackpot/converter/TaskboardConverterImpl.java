package stackpot.stackpot.converter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.Taskboard;
import stackpot.stackpot.domain.mapping.PotMember;
import stackpot.stackpot.web.dto.MyPotTaskRequestDto;
import stackpot.stackpot.web.dto.MyPotTaskResponseDto;

import java.util.List;
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
        return new MyPotTaskResponseDto.Participant(
                participant.getUser().getId(),
                participant.getPotMemberId(),
                participant.getUser().getNickname(),
                participant.getUser().getEmail()
        );
    }
}
