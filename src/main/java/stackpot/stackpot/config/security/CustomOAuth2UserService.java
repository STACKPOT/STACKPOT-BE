package stackpot.stackpot.config.security;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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

//    @Transactional
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//        // 1. 유저 정보(attributes) 가져오기
//        Map<String, Object> oAuth2UserAttributes = super.loadUser(userRequest).getAttributes();
//
//        // 2. resistrationId 가져오기 (third-party id)
//        String registrationId = userRequest.getClientRegistration().getRegistrationId();
//
//        // 3. userNameAttributeName 가져오기
//        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
//                .getUserInfoEndpoint().getUserNameAttributeName();
//
//        // 4. 유저 정보 dto 생성
//        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.of(registrationId, oAuth2UserAttributes);
//
//        // 5. 회원가입 및 로그인
//        User user = saveOrUpdateUser(oAuth2UserInfo);
//
//        // 6. OAuth2User로 반환
//        return new PrincipalDetails(user, oAuth2UserAttributes, userNameAttributeName);
//    }
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println("loadUser");

        // 사용자 정보 가져오기
        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        if (kakaoAccount == null) {
            throw new OAuth2AuthenticationException("Failed to retrieve kakao_account from attributes.");
        }

        // 이메일 가져오기
        String email = (String) kakaoAccount.get("email");
        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found in kakao_account.");
        }

        // 사용자 정보 확인
        userRepository.findByEmail(email).ifPresentOrElse(
                user -> System.out.println("Existing user found: " + email),
                () -> System.out.println("No user found. Redirecting to signup.")
        );

        // attributes에 email 추가
        Map<String, Object> modifiedAttributes = new HashMap<>(attributes);
        modifiedAttributes.put("email", email);

        // 디버깅: modifiedAttributes 확인
        System.out.println("Final Modified Attributes: " + modifiedAttributes);

         saveOrUpdateUser(email);


        // DefaultOAuth2User 생성
        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                modifiedAttributes,
                "email" // Principal로 사용할 필드 이름
        );
    }

    private void saveOrUpdateUser(String email) {
        System.out.println("saveOrUpdateUser 실행");
        userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User user = User.builder()
                            .email(email)
                            .userTemperature(33)
                            .build();
                    return userRepository.save(user); // 저장된 사용자 반환
                });
    }
}