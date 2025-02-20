package stackpot.stackpot.converter;


import stackpot.stackpot.domain.Feed;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.Taskboard;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.domain.mapping.UserTodo;
import stackpot.stackpot.web.dto.*;

import java.util.List;

public interface MyPotConverter {
    OngoingPotResponseDto convertToOngoingPotResponseDto(Pot pot, Long userId);
    CompletedPotBadgeResponseDto toCompletedPotBadgeResponseDto(Pot pot, String formattedMembers, Role userPotRole, List<BadgeDto> myBadges);
    RecruitingPotResponseDto convertToRecruitingPotResponseDto(Pot pot, Long userId);
    MyPotTodoResponseDTO toDto(User member, Pot pot, List<UserTodo> userTodos, User currentUSer);
}
