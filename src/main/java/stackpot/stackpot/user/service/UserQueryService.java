package stackpot.stackpot.user.service;

import stackpot.stackpot.user.entity.enums.Role;

public interface UserQueryService {

    String selectNameByUserId(Long userId);
}
