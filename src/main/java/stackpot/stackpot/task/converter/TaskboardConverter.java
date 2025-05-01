package stackpot.stackpot.task.converter;

import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.task.entity.Taskboard;
import stackpot.stackpot.task.entity.enums.TaskboardStatus;
import stackpot.stackpot.pot.entity.mapping.PotMember;
import stackpot.stackpot.task.dto.MyPotTaskPreViewResponseDto;
import stackpot.stackpot.task.dto.MyPotTaskRequestDto;
import stackpot.stackpot.task.dto.MyPotTaskResponseDto;
import stackpot.stackpot.task.dto.MyPotTaskStatusResponseDto;

import java.util.List;

public interface TaskboardConverter {
    Taskboard toTaskboard(Pot pot , MyPotTaskRequestDto.create requset);
    MyPotTaskResponseDto toDTO(Taskboard taskboard, List<PotMember> participants);
    List<MyPotTaskResponseDto.Participant> toParticipantDtoList(List<PotMember> participants);
    MyPotTaskResponseDto.Participant toParticipantDto(PotMember participant);
    MyPotTaskPreViewResponseDto toDto(Taskboard taskboard, List<PotMember> participants);
    MyPotTaskStatusResponseDto toTaskStatusDto(Taskboard taskboard, TaskboardStatus taskboardStatus);

}
