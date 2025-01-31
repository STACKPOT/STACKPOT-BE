package stackpot.stackpot.converter;

import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.Taskboard;
import stackpot.stackpot.domain.mapping.PotMember;
import stackpot.stackpot.web.dto.MyPotTaskPreViewResponseDto;
import stackpot.stackpot.web.dto.MyPotTaskRequestDto;
import stackpot.stackpot.web.dto.MyPotTaskResponseDto;

import java.util.List;

public interface TaskboardConverter {
    Taskboard toTaskboard(Pot pot , MyPotTaskRequestDto.create requset);
    MyPotTaskResponseDto toDTO(Taskboard taskboard);
    List<MyPotTaskResponseDto.Participant> toParticipantDtoList(List<PotMember> participants);
    MyPotTaskResponseDto.Participant toParticipantDto(PotMember participant);
    MyPotTaskPreViewResponseDto toDto(Taskboard taskboard, List<PotMember> participants);
}
