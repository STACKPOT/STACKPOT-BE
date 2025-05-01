package stackpot.stackpot.pot.converter;


import stackpot.stackpot.badge.dto.BadgeDto;
import stackpot.stackpot.badge.dto.CompletedPotBadgeResponseDto;
import stackpot.stackpot.pot.dto.OngoingPotResponseDto;
import stackpot.stackpot.pot.dto.RecruitingPotResponseDto;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.todo.dto.MyPotTodoResponseDTO;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.entity.enums.Role;
import stackpot.stackpot.todo.entity.mapping.UserTodo;

import java.util.List;

public interface MyPotConverter {
    OngoingPotResponseDto convertToOngoingPotResponseDto(Pot pot, Long userId);
    CompletedPotBadgeResponseDto toCompletedPotBadgeResponseDto(Pot pot, String formattedMembers, Role userPotRole, List<BadgeDto> myBadges);
    RecruitingPotResponseDto convertToRecruitingPotResponseDto(Pot pot, Long userId);
    MyPotTodoResponseDTO toDto(User member, Pot pot, List<UserTodo> userTodos, User currentUSer);
}
