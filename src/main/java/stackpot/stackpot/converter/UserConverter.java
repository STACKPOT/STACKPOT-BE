package stackpot.stackpot.converter;

import stackpot.stackpot.domain.User;
import stackpot.stackpot.web.dto.UserRequestDTO;

public class UserConverter {
    public static User toUser(UserRequestDTO.JoinDto request) {

        return User.builder()
                .nickname(request.getNickname())
                .email(request.getEmail())   // 추가된 코드
                .kakaoId(request.getKakaoId())
                .interest(request.getInterest())
                .role(request.getRole())
                .userTemperature((int)35.5)
                .build();
    }
}

