package stackpot.stackpot.converter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.Taskboard;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.domain.mapping.PotMember;
import stackpot.stackpot.web.dto.MyPotTaskPreViewResponseDto;
import stackpot.stackpot.web.dto.MyPotTaskRequestDto;
import stackpot.stackpot.web.dto.MyPotTaskResponseDto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TaskboardConverterImpl implements TaskboardConverter{

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy. MM. dd");
    @Override
    public Taskboard toTaskboard(Pot pot, MyPotTaskRequestDto.create requset) {
        return Taskboard.builder()
                .title(requset.getTitle())
                .description(requset.getDescription())
                .deadLine(requset.getDeadline())
                .status(requset.getTaskboardStatus())
                .pot(pot)
                .build();
    }
    @Override
    public MyPotTaskResponseDto toDTO(Taskboard taskboard) {
        return MyPotTaskResponseDto.builder()
                .taskboardId(taskboard.getTaskboardId())
                .creatorNickname(taskboard.getUser().getNickname()+getVegetableNameByRole(String.valueOf(taskboard.getUser().getRole())))
                .creatorRole(taskboard.getUser().getRole())
                .deadLine(formatDate(taskboard.getDeadLine()))
                .dDay(dDayCount(taskboard.getDeadLine()))
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
                .nickName(participant.getUser().getNickname() + getVegetableNameByRole(participant.getRoleName().toString()))
                .role(participant.getRoleName())
                .build();
    }

    public MyPotTaskPreViewResponseDto toDto(Taskboard taskboard, List<PotMember> participants) {

        return MyPotTaskPreViewResponseDto.builder()
                .taskboardId(taskboard.getTaskboardId())
                .title(taskboard.getTitle())
                .creatorNickname(taskboard.getUser().getNickname()+getVegetableNameByRole(String.valueOf(taskboard.getUser().getRole())))
                .creatorRole(taskboard.getUser().getRole())
                .dDay(dDayCount(taskboard.getDeadLine()))
                .description(taskboard.getDescription())
                .category(determineCategories(participants)) // 카테고리 설정
                .status(taskboard.getStatus()) // OPEN, IN_PROGRESS, CLOSED
                .deadLine(formatDate(taskboard.getDeadLine()))
                .participants(toParticipantDtoList(participants)) // 참여자 리스트 변환
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

    private List<Role> determineCategories(List<PotMember> participants) {
        return participants.stream()
                .map(PotMember::getRoleName) // PotMember의 roleName 추출
                .distinct() // 중복 제거
                .collect(Collectors.toList()); // 리스트로 변환
    }

    private String formatDate(java.time.LocalDate date) {
        return (date != null) ? date.format(DATE_FORMATTER) : "N/A";
    }

    private String dDayCount(LocalDate deadLine){
        LocalDate today = LocalDate.now();
        LocalDate deadline = deadLine;

        long daysDiff = ChronoUnit.DAYS.between(today, deadline);

        String dDay;
        if (daysDiff == 0) {
            dDay = "D-Day";
        } else if (daysDiff > 0) {
            dDay = "D-" + daysDiff;
        } else {
            dDay = "D+" + Math.abs(daysDiff);
        }
        return dDay;
    }
}
