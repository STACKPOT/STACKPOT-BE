package stackpot.stackpot.user.service;

import stackpot.stackpot.user.dto.response.MyDescriptionResponseDto;
import stackpot.stackpot.user.entity.enums.Role;

public interface UserQueryService {

    String selectNameByUserId(Long userId);
    MyDescriptionResponseDto getMyDescription();
    MyDescriptionResponseDto getUserDescription(Long userId);
}
