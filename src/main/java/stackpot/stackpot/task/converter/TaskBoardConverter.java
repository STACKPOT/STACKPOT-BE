package stackpot.stackpot.task.converter;

import stackpot.stackpot.common.util.DateFormatter;
import stackpot.stackpot.common.util.DdayCounter;
import stackpot.stackpot.common.util.RoleNameMapper;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.task.entity.Taskboard;
import stackpot.stackpot.task.entity.enums.TaskboardStatus;
import stackpot.stackpot.pot.entity.mapping.PotMember;
import stackpot.stackpot.task.dto.MyPotTaskPreViewResponseDto;
import stackpot.stackpot.task.dto.MyPotTaskRequestDto;
import stackpot.stackpot.task.dto.MyPotTaskResponseDto;
import stackpot.stackpot.task.dto.MyPotTaskStatusResponseDto;

import java.util.List;

import org.springframework.stereotype.Component;
import stackpot.stackpot.user.entity.enums.Role;


import java.util.stream.Collectors;

@Component
public class TaskBoardConverter {
    public Taskboard toTaskboard(Pot pot, MyPotTaskRequestDto.create requset) {
        return Taskboard.builder()
                .title(requset.getTitle())
                .description(requset.getDescription())
                .deadLine(requset.getDeadline())
                .status(requset.getTaskboardStatus())
                .pot(pot)
                .build();
    }
    public MyPotTaskResponseDto toDTO(Taskboard taskboard, List<PotMember> participants) {
        return MyPotTaskResponseDto.builder()
                .taskboardId(taskboard.getTaskboardId())
                .title(taskboard.getTitle())
                .creatorUserId(taskboard.getUser().getUserId())
                .creatorNickname(taskboard.getUser().getNickname() + " " + RoleNameMapper.mapRoleName(String.valueOf(taskboard.getUser().getRole())))
                .creatorRole(taskboard.getUser().getRole())
                .deadLine(DateFormatter.dotFormatter(taskboard.getDeadLine()))
                .dDay(DdayCounter.dDayCount(taskboard.getDeadLine()))
                .description(taskboard.getDescription())
                .status(taskboard.getStatus())
                .potId(taskboard.getPot().getPotId())
                .participants(toParticipantDtoList(participants)) // 참여자 리스트 변환
                .build();
    }
    public List<MyPotTaskResponseDto.Participant> toParticipantDtoList(List<PotMember> participants) {
        return participants.stream()
                .map(this::toParticipantDto)
                .collect(Collectors.toList());
    }

    public MyPotTaskResponseDto.Participant toParticipantDto(PotMember participant) {

        return MyPotTaskResponseDto.Participant.builder()
                .potMemberId(participant.getPotMemberId())
                .userId(participant.getUser().getId())
                .nickName(participant.getUser().getNickname() + " " + RoleNameMapper.mapRoleName(participant.getRoleName().toString()))
                .role(participant.getRoleName())
                .build();
    }

    public MyPotTaskPreViewResponseDto toDto(Taskboard taskboard, List<PotMember> participants) {
        return MyPotTaskPreViewResponseDto.builder()
                .taskboardId(taskboard.getTaskboardId())
                .title(taskboard.getTitle())
                .creatorNickname(taskboard.getUser().getNickname() + " " + RoleNameMapper.mapRoleName(String.valueOf(taskboard.getUser().getRole())))
                .creatorRole(taskboard.getUser().getRole())
                .dDay(DdayCounter.dDayCount(taskboard.getDeadLine()))
                .description(taskboard.getDescription())
                .category(determineCategories(participants)) // 카테고리 설정
                .status(taskboard.getStatus()) // OPEN, IN_PROGRESS, CLOSED
                .deadLine(DateFormatter.dotFormatter(taskboard.getDeadLine()))
                .participants(toParticipantDtoList(participants)) // 참여자 리스트 변환
                .build();
    }

    public MyPotTaskStatusResponseDto toTaskStatusDto(Taskboard taskboard, TaskboardStatus taskboardStatus) {
        return MyPotTaskStatusResponseDto.builder()
                .taskboardId(taskboard.getTaskboardId())
                .title(taskboard.getTitle())
                .status(taskboard.getStatus()) // OPEN, IN_PROGRESS, CLOSED
                .build();
    }


    private List<Role> determineCategories(List<PotMember> participants) {
        return participants.stream()
                .map(PotMember::getRoleName) // PotMember의 roleName 추출
                .distinct() // 중복 제거
                .collect(Collectors.toList()); // 리스트로 변환
    }



}
