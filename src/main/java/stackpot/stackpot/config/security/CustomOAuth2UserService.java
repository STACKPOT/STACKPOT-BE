package stackpot.stackpot.config.security;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.repository.UserRepository.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");

        String email = (String) properties.get("email");

        // 사용자 정보 조회
        User user = userRepository.findByEmail(email).orElse(null);

        // 리다이렉션 상태 플래그 추가
        Map<String, Object> modifiedAttributes = new HashMap<>(attributes);
        modifiedAttributes.put("email", email);
        if (user == null) {
            // 회원가입 페이지로 리다이렉션
            modifiedAttributes.put("redirect", String.format("/signup?email=%s", email));
        } else {
            // 홈 페이지로 리다이렉션
            modifiedAttributes.put("redirect", "/home");
        }

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                modifiedAttributes,
                "email"  // Principal로 설정
        );
    }

    private User saveOrUpdateUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElse(User.builder()
                        .email(email)
                        .kakaoId(passwordEncoder.encode("OAUTH_USER_" + UUID.randomUUID()))
                        .build());

        return userRepository.save(user);
    }
}